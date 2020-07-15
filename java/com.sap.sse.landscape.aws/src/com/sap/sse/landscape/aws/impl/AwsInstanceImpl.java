package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.AvailabilityZone;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.Metrics;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.SshShellCommandChannel;
import com.sap.sse.landscape.ssh.JCraftLogAdapter;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.landscape.ssh.YesUserInfo;

import software.amazon.awssdk.services.ec2.model.Instance;

public class AwsInstanceImpl implements AwsInstance {
    private final static Logger logger = Logger.getLogger(AwsInstanceImpl.class.getName());
    private static final String ROOT_USER_NAME = "root";
    private final String instanceId;
    private final AwsAvailabilityZone availabilityZone;
    private final AwsLandscape<?, ?> landscape;
    
    public AwsInstanceImpl(String instanceId, AwsAvailabilityZone availabilityZone, AwsLandscape<?, ?> landscape) {
        this.instanceId = instanceId;
        this.availabilityZone = availabilityZone;
        this.landscape = landscape;
    }
    
    /**
     * Obtains a fresh copy of the instance by looking it up in the {@link #getRegion() region} by its {@link #instanceId ID}.
     */
    private Instance getInstance() {
        return landscape.getInstance(getInstanceId(), availabilityZone.getRegion());
    }

    @Override
    public InetAddress getPublicAddress() {
        try {
            final String publicIpAddress = getInstance().publicIpAddress();
            return publicIpAddress==null?null:InetAddress.getByName(publicIpAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InetAddress getPublicAddress(Duration timeoutNullMeaningForever) {
        InetAddress result;
        final TimePoint started = TimePoint.now();
        while ((result = getPublicAddress()) == null &&
                (timeoutNullMeaningForever == null ||
                 started.until(TimePoint.now()).compareTo(timeoutNullMeaningForever) < 0));
        return result;
    }

    /**
     * Establishes an unconnected session configured for the "root" user.
     * 
     * @see #createRootSshChannel
     */
    public com.jcraft.jsch.Session createRootSshSession() throws JSchException {
        return createSshSession(ROOT_USER_NAME);
    }
    
    /**
     * Establishes an unconnected session configured for the "root" user.
     * 
     * @see #createRootSshChannel
     */
    public com.jcraft.jsch.Session createSshSession(String sshUserName) throws JSchException {
        final String keyName = getInstance().keyName(); // the SSH key pair name that can be used to log on
        final SSHKeyPair keyPair = landscape.getSSHKeyPair(getRegion(), keyName);
        final JSch jsch = new JSch();
        JSch.setLogger(new JCraftLogAdapter());
        jsch.addIdentity(keyName, landscape.getDescryptedPrivateKey(keyPair), keyPair.getPublicKey(), /* TODO passphrase */ null);
        final InetAddress address = getPublicAddress();
        if (address == null) {
            throw new IllegalStateException("Instance "+getInstanceId()+" doesn't have a public IP address");
        }
        return jsch.getSession(sshUserName, address.getHostAddress());
    }

    /**
     * Connects to an SSH session for the "root" user with a "shell" channel
     * 
     * @see #createSshChannel(String)
     */
    @Override
    public SshShellCommandChannel createRootSshChannel() throws JSchException, IOException, InterruptedException {
        return createSshChannel(ROOT_USER_NAME);
    }
    
    /**
     * Connects to an SSH session for the username specified and opens a "shell" channel. Use the {@link Channel}
     * returned by {@link Channel#setInputStream(java.io.InputStream) setting an input stream} from which the commands
     * to be sent to the server will be read, and by {@link Channel#setOutputStream(java.io.OutputStream) setting the
     * output stream} to which the server will send its output. You will usually want to use either a
     * {@link ByteArrayInputStream} to provide a set of predefined commands to sent to the server, and a
     * {@link PipedInputStream} wrapped around a {@link PipedOutputStream} which you set to the channel.
     */
    @Override
    public SshShellCommandChannel createSshChannel(String sshUserName) throws JSchException, IOException, InterruptedException {
        // TODO use "exec" channel type for plain command execution, producing a ChannelExec instance
        return new SshShellCommandChannelImpl(createSshChannelInternal(sshUserName, "shell"));
    }
    
    private Channel createSshChannelInternal(String sshUserName, String channelType) throws JSchException, IOException {
        logger.info("Creating SSH "+channelType+" channel for SSH user "+sshUserName+
                " to instance with ID "+getInstanceId());
        final Session session = createSshSession(sshUserName);
        session.setUserInfo(new YesUserInfo());
        session.connect(/* timeout in millis */ 5000);
        final Channel channel = session.openChannel(channelType);
        return channel;
    }
    
    @Override
    public ChannelSftp createSftpChannel(String sshUserName) throws JSchException, IOException {
        return (ChannelSftp) createSshChannelInternal(sshUserName, "sftp");
    }

    @Override
    public ChannelSftp createRootSftpChannel() throws JSchException, IOException {
        return createSftpChannel(ROOT_USER_NAME);
    }

    @Override
    public Iterable<? extends Process<? extends Log, ? extends Metrics>> getRunningProcesses() {
        // TODO Implement AwsInstance.getRunningProcesses(...)
        return null;
    }

    @Override
    public long getPhysicalRamInBytes() {
        // TODO Implement AwsInstance.getPhysicalRamInBytes(...)
        return 0;
    }

    @Override
    public long getVirtualMemoryInBytes() {
        // TODO Implement AwsInstance.getVirtualMemoryInBytes(...)
        return 0;
    }

    @Override
    public int getNumberOfCPUs() {
        return getInstance().cpuOptions().coreCount();
    }

    @Override
    public long getNetworkBandwidthInBytesPerSecond() {
        // TODO Implement AwsInstance.getNetworkBandwidthInBytesPerSecond(...)
        return 0;
    }

    @Override
    public AvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    private AwsRegion getRegion() {
        return availabilityZone.getRegion();
    }

    @Override
    public Iterable<SecurityGroup> getSecurityGroups() {
        // TODO Implement AwsInstance.getSecurityGroups(...)
        return null;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }
}
