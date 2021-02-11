package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.ssh.JCraftLogAdapter;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.landscape.ssh.SshCommandChannel;
import com.sap.sse.landscape.ssh.SshCommandChannelImpl;
import com.sap.sse.landscape.ssh.YesUserInfo;
import com.sap.sse.util.Wait;

import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

public class AwsInstanceImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics> implements AwsInstance<ShardingKey, MetricsT> {
    private final static Logger logger = Logger.getLogger(AwsInstanceImpl.class.getName());
    private static final String ROOT_USER_NAME = "root";
    private final String instanceId;
    private final AwsAvailabilityZone availabilityZone;
    private final AwsLandscape<ShardingKey, MetricsT, ?> landscape;
    
    public AwsInstanceImpl(String instanceId, AwsAvailabilityZone availabilityZone, AwsLandscape<ShardingKey, MetricsT, ?> landscape) {
        this.instanceId = instanceId;
        this.availabilityZone = availabilityZone;
        this.landscape = landscape;
    }
    
    @Override
    public boolean equals(Object other) {
        @SuppressWarnings("unchecked")
        AwsInstance<?, ? extends ApplicationProcessMetrics> otherCast = (AwsInstance<?, ? extends ApplicationProcessMetrics>) other;
        return otherCast.getInstanceId().equals(getInstanceId());
    }

    @Override
    public int hashCode() {
        return getInstance().hashCode();
    }
    
    /**
     * Obtains a fresh copy of the instance by looking it up in the {@link #getRegion() region} by its {@link #instanceId ID}.
     */
    private Instance getInstance() {
        return landscape.getInstance(getInstanceId(), getRegion());
    }

    @Override
    public InetAddress getPublicAddress() {
        return getIpAddress(Instance::publicIpAddress);
    }
    
    @Override
    public InetAddress getPublicAddress(Optional<Duration> timeoutEmptyMeaningForever) {
        return getAddressWithTimeout(timeoutEmptyMeaningForever, this::getPublicAddress);
    }
    
    @Override
    public InetAddress getPrivateAddress() {
        return getIpAddress(Instance::privateIpAddress);
    }
    
    @Override
    public InetAddress getPrivateAddress(Optional<Duration> timeoutEmptyMeaningForever) {
        return getAddressWithTimeout(timeoutEmptyMeaningForever, this::getPrivateAddress);
    }

    private InetAddress getIpAddress(Function<Instance, String> addressAsStringSupplier) {
        try {
            final Instance instance = getInstance();
            final InetAddress result;
            if (instance.state().name() == InstanceStateName.RUNNING) {
                final String privateIpAddress = addressAsStringSupplier.apply(instance);
                result = privateIpAddress==null?null:InetAddress.getByName(privateIpAddress);
            } else {
                result = null; // not RUNNING
            }
            return result;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private InetAddress getAddressWithTimeout(Optional<Duration> timeoutNullMeaningForever, Supplier<InetAddress> addressSupplierMethod) {
        final Instance instance = getInstance();
        InetAddress result;
        // for RUNNING and PENDING instances it's worthwhile waiting for the address to show; in all other cases we return null immediately
        if (instance.state().name() == InstanceStateName.RUNNING || instance.state().name() == InstanceStateName.PENDING) {
            final TimePoint started = TimePoint.now();
            while ((result = addressSupplierMethod.get()) == null &&
                    (!timeoutNullMeaningForever.isPresent() ||
                     started.until(TimePoint.now()).compareTo(timeoutNullMeaningForever.get()) < 0));
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Establishes an unconnected session configured for the "root" user.
     * 
     * @param optionalKeyName
     *            the name of the SSH key pair to use to log on; must identify a key pair available for the
     *            {@link #getRegion() region} of this instance. If not provided, the the SSH private key for the key
     *            pair that was originally used when the instance was launched will be used.
     * @param privateKeyEncryptionPassphrase
     *            the pass phrase for the private key that belongs to the instance's public key used for start-up
     * @see #createRootSshChannel
     */
    public com.jcraft.jsch.Session createRootSshSession(Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws JSchException {
        return createSshSession(ROOT_USER_NAME, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    /**
     * Establishes an unconnected session configured for the user specified by {@code sshUserName}, trying to find and
     * unlock the SSH private key for the key pair whose name is provided by the {@code keyName} parameter. A
     * {@link NullPointerException} will be thrown if such a key cannot be found in the {@link #getRegion() region} of
     * this instance.
     * 
     * @param optionalKeyName
     *            the name of the SSH key pair to use to log on; must identify a key pair available for the
     *            {@link #getRegion() region} of this instance. If not provided, the the SSH private key for the key
     *            pair that was originally used when the instance was launched will be used.
     * @param privateKeyEncryptionPassphrase
     *            the pass phrase to unlock the private key that belongs to the key pair identified by {@code keyName}
     * @see #createRootSshChannel
     */
    public com.jcraft.jsch.Session createSshSession(String sshUserName, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws JSchException {
        final String keyName = optionalKeyName.orElseGet(()->getInstance().keyName()); // the SSH key pair name that can be used to log on
        final SSHKeyPair keyPair = landscape.getSSHKeyPair(getRegion(), keyName);
        final JSch jsch = new JSch();
        JSch.setLogger(new JCraftLogAdapter());
        jsch.addIdentity(keyName, landscape.getDecryptedPrivateKey(keyPair, privateKeyEncryptionPassphrase), keyPair.getPublicKey(), /* passphrase */ null);
        final InetAddress address = getPublicAddress();
        if (address == null) {
            throw new IllegalStateException("Instance "+getInstanceId()+" doesn't have a public IP address");
        }
        return jsch.getSession(sshUserName, address.getHostAddress());
    }

    /**
     * Connects to an SSH session for the "root" user with a "shell" channel
     * @param privateKeyEncryptionPassphrase
     *            the pass phrase for the private key that belongs to the instance's public key used for start-up
     * 
     * @see #createSshChannel(String, Optional, byte[])
     */
    @Override
    public SshCommandChannel createRootSshChannel(Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return createSshChannel(ROOT_USER_NAME, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    /**
     * Connects to an SSH session for the username specified and opens a "shell" channel. Use the {@link Channel}
     * returned by {@link Channel#setInputStream(java.io.InputStream) setting an input stream} from which the commands
     * to be sent to the server will be read, and by {@link Channel#setOutputStream(java.io.OutputStream) setting the
     * output stream} to which the server will send its output. You will usually want to use either a
     * {@link ByteArrayInputStream} to provide a set of predefined commands to sent to the server, and a
     * {@link PipedInputStream} wrapped around a {@link PipedOutputStream} which you set to the channel.
     * 
     * @param privateKeyEncryptionPassphrase
     *            the pass phrase for the private key that belongs to the instance's public key used for start-up
     */
    @Override
    public SshCommandChannel createSshChannel(String sshUserName, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return new SshCommandChannelImpl((ChannelExec) createSshChannelInternal(sshUserName, "exec", optionalTimeout,
                optionalKeyName, privateKeyEncryptionPassphrase));
    }
    
    private Channel createSshChannelInternal(String sshUserName, String channelType, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        logger.info(
                "Creating SSH "+channelType+" channel for SSH user "+sshUserName+
                " to instance with ID "+getInstanceId());
        Channel result;
        try {
            result = Wait.wait(()->{
                    Session session = null;
                    try {
                        session = createSshSession(sshUserName, optionalKeyName, privateKeyEncryptionPassphrase);
                        session.setUserInfo(new YesUserInfo());
                        session.connect(optionalTimeout.map(d->d.asMillis()).orElse(0l).intValue());
                        return session.openChannel(channelType);
                    } catch (JSchException | IllegalStateException e) {
                        if (session != null) {
                            session.disconnect();
                        }
                        throw e;
                    }
                },
                channel->channel != null,
                /* retryOnException */ true, optionalTimeout,
                Duration.ONE_SECOND.times(5), Level.INFO,
                "Trying to connect to " + getInstanceId() + " with user " + sshUserName + " using SSH");
        } catch (TimeoutException timeout) {
            result = null;
        }
        return result;
    }
    
    @Override
    public ChannelSftp createSftpChannel(String sshUserName, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return (ChannelSftp) createSshChannelInternal(sshUserName, "sftp", optionalTimeout,
                optionalKeyName, privateKeyEncryptionPassphrase);
    }

    @Override
    public ChannelSftp createRootSftpChannel(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        return createSftpChannel(ROOT_USER_NAME, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }

    @Override
    public AwsAvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    @Override
    public Iterable<SecurityGroup> getSecurityGroups() {
        return Util.map(getInstance().securityGroups(), groupIdentifier -> 
            landscape.getSecurityGroup(groupIdentifier.groupId(), getRegion()));
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }
    
    @Override
    public void terminate() {
        landscape.terminate(this);
    }
    
    protected AwsLandscape<ShardingKey, MetricsT, ?> getLandscape() {
        return landscape;
    }
    
    @Override
    public String toString() {
        return getInstanceId();
    }
}
