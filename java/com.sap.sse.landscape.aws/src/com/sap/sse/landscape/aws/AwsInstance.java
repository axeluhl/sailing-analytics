package com.sap.sse.landscape.aws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.sap.sse.landscape.Host;

public interface AwsInstance extends Host {
    /**
     * Connects to an SSH session for the username specified, using the SSH key pair used to launch the instance, and
     * opens a "shell" channel. Use the {@link Channel} returned by {@link Channel#setInputStream(java.io.InputStream)
     * setting an input stream} from which the commands to be sent to the server will be read, and by
     * {@link Channel#setOutputStream(java.io.OutputStream) setting the output stream} to which the server will send its
     * output. You will usually want to use either a {@link ByteArrayInputStream} to provide a set of predefined
     * commands to sent to the server, and a {@link PipedInputStream} wrapped around a {@link PipedOutputStream} which
     * you set to the channel.
     */
    Channel createSshChannel(String sshUserName) throws JSchException;

    /**
     * Connects to an SSH session for the "root" user with a "shell" channel
     * 
     * @see #createSshChannel(String)
     */
    Channel createRootSshChannel() throws JSchException;

    String getInstanceId();

    /**
     * After having obtained a {@link Channel} from {@link #createSshChannel(String)} or
     * {@link #createRootSshChannel()}, the shell may take some time to start and become responsive. This method will
     * assume the channel passed as {@code sshShellChannel} is not yet {@link Channel#connect() connected}, and obtains
     * the channel's {@link Channel#getOutputStream()}, then connects and uses the streams to send an "echo" command
     * with a generated "stanza" which is then waited for on the channel's {@link Channel#getInputStream() input
     * stream}.
     */
    void waitUntilShellResponse(Channel sshShellChannel) throws IOException, JSchException;
}
