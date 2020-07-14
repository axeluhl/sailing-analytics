package com.sap.sse.landscape.aws;

import java.io.IOException;

/**
 * A channel to an SSH shell session through which commands can be issued
 * @author Axel Uhl (D043530)
 *
 */
public interface SshShellCommandChannel {
    /**
     * @param commandLine
     *            the command without any trailing line separator
     * @return the bytes, without the echoed command line itself, received on standard output, excluding the next prompt
     *         but including any terminating line separator; note that line separators are not mapped in any way.
     */
    byte[] sendCommandLineSynchronously(String commandLine) throws IOException, InterruptedException;
    
    void disconnect();
}
