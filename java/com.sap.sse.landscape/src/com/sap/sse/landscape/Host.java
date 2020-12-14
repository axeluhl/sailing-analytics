package com.sap.sse.landscape;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Optional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.ssh.SshCommandChannel;

public interface Host {
    /**
     * Obtains the public IP address of this host. Note that during the boot phase and after shutdown/termination a host
     * may not yet have such a public IP address assigned. In this case, {@code null} may be returned. To avoid this,
     * you can alternatively call {@link #getPublicAddress(Optional)} to wait for a public IP address to become
     * available.
     */
    InetAddress getPublicAddress();
    
    /**
     * Obtains the public IP address of this host, waiting for one to become available for the duration of
     * {@code timeout}, or forever in case {@code timeout} is {@code null}
     * 
     * @param timeoutEmptyMeaningForever
     *            if {@code null}, waits forever
     */
    InetAddress getPublicAddress(Optional<Duration> timeoutEmptyMeaningForever);
    
    /**
     * Obtains the private IP address of this host. Note that during the boot phase and after shutdown/termination a host
     * may not yet have such a public IP address assigned. In this case, {@code null} may be returned. To avoid this,
     * you can alternatively call {@link #getPrivateAddress(Optional)} to wait for a public IP address to become
     * available.
     */
    InetAddress getPrivateAddress();

    /**
     * Obtains the private IP address of this host, waiting for one to become available for the duration of
     * {@code timeout}, or forever in case {@code timeout} is {@code null}
     * 
     * @param timeoutEmptyMeaningForever
     *            if {@code null}, waits forever
     */
    InetAddress getPrivateAddress(Optional<Duration> timeoutEmptyMeaningForever);

    /**
     * Connects to an SSH session for the username specified, using the SSH key pair used to launch the instance, and
     * opens a "shell" channel. Use the {@link Channel} returned by {@link Channel#setInputStream(java.io.InputStream)
     * setting an input stream} from which the commands to be sent to the server will be read, and by
     * {@link Channel#setOutputStream(java.io.OutputStream) setting the output stream} to which the server will send its
     * output. You will usually want to use either a {@link ByteArrayInputStream} to provide a set of predefined
     * commands to sent to the server, and a {@link PipedInputStream} wrapped around a {@link PipedOutputStream} which
     * you set to the channel.
     */
    SshCommandChannel createSshChannel(String sshUserName, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException;

    /**
     * Connects to an SSH session for the "root" user with a "shell" channel
     * 
     * @see #createSshChannel(String, Optional)
     */
    SshCommandChannel createRootSshChannel(Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException;
    
    ChannelSftp createSftpChannel(String sshUserName, Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException;

    ChannelSftp createRootSftpChannel(Optional<Duration> optionalTimeout) throws JSchException, IOException, InterruptedException;
    
    /**
     * Tells where in the cloud this host runs; the availability zone {@link AvailabilityZone#getRegion() implies} the
     * {@link Region}.
     */
    AvailabilityZone getAvailabilityZone();
    
    default Region getRegion() {
        return getAvailabilityZone().getRegion();
    }
    
    Iterable<SecurityGroup> getSecurityGroups();
}
