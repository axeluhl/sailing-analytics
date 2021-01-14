package com.sap.sse.landscape.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import com.jcraft.jsch.JSchException;

/**
 * A channel to an SSH shell session through which commands can be issued
 * @author Axel Uhl (D043530)
 *
 */
public interface SshCommandChannel {
    /**
     * Executes the {@code commandLine}, then {@link #disconnect() disconnects} this channel. Any standard error content
     * will be logged with the {@code stderrLogLevel} specified or will be ignored when the {@code stderrLogLevel} is
     * {@code null}, prefixed with the {@code stderrLogPrefix}. The standard output will be returned as a single
     * {@link String}.
     * 
     * @param stderrLogPrefix
     *            may be {@code null}, meaning no prefix
     * @param stderrLogLevel
     *            {@code null} means not to log stderr at all
     */
    String runCommandAndReturnStdoutAndLogStderr(String commandLine, String stderrLogPrefix, Level stderrLogLevel) throws IOException, InterruptedException, JSchException;
    
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

    /**
     * Consumes the entire input stream content as provided by {@link #sendCommandLineSynchronously(String, OutputStream)} and then
     * disconnects and closes the channel.
     */
    String getStreamContentsAsString() throws IOException, JSchException;
}
