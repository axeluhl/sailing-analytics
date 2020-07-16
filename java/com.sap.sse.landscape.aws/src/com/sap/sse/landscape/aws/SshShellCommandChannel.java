package com.sap.sse.landscape.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.JSchException;

/**
 * A channel to an SSH shell session through which commands can be issued
 * @author Axel Uhl (D043530)
 *
 */
public interface SshShellCommandChannel {
    /**
     * @param commandLine
     *            the command without any trailing line separator
     * @param stderr
     *            the output stream to which standard error output produced during command execution will be sent
     * @return the stream from which the caller can read the standard output produced by the command. Clients should
     *         consume this stream up to its end. Afterwards, {@link #getExitStatus()} can be used to obtain the last
     *         command's exit status. See also {@link #getStreamContentsAsByteArray()}.
     */
    InputStream sendCommandLineSynchronously(String commandLine, OutputStream stderr) throws IOException, InterruptedException, JSchException;
    
    int getExitStatus();
    
    void disconnect();

    /**
     * Consumes the entire input stream content as provided by {@link #sendCommandLineSynchronously(String, OutputStream)} and then
     * disconnects and closes the channel.
     */
    byte[] getStreamContentsAsByteArray() throws IOException, JSchException;
}
