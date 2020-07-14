package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.SshShellCommandChannel;


public class SshShellCommandChannelImpl implements SshShellCommandChannel {
    private static final Pattern DEFAULT_PROMPT_PATTERN = Pattern.compile("^\\[.*\\]# $", Pattern.MULTILINE);
    private final Channel channel;
    private final Pattern promptPattern;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    /**
     * @param channel
     *            the result of a {@link Session#openChannel(String)} call; the channel is assumed to not yet be
     *            {@link Channel#connect() connected}
     */
    public SshShellCommandChannelImpl(Channel channel) throws IOException, JSchException {
        this(channel, DEFAULT_PROMPT_PATTERN);
    }
    
    public SshShellCommandChannelImpl(Channel channel, Pattern promptPattern) throws IOException, JSchException {
        super();
        this.channel = channel;
        this.promptPattern = promptPattern;
        final Pair<InputStream, OutputStream> streams = waitUntilShellResponse();
        this.inputStream = streams.getA();
        this.outputStream = streams.getB();
    }

    @Override
    public byte[] sendCommandLineSynchronously(String commandLine) throws IOException, InterruptedException {
        new Thread(()->{
            try {
                outputStream.write((commandLine+"\n").getBytes());
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        // wait a bit for echoed command to become available:
        int attempts = 10;
        while (inputStream.available() == 0 && --attempts > 0) {
            Thread.sleep(10);
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ByteArrayOutputStream echo = new ByteArrayOutputStream();
        // assume the command line got echoed; read it because we don't want it as part of the output
        for (int i=0; i<commandLine.length() && inputStream.available() > 0; i++) {
            echo.write(inputStream.read());
        }
        int read = readFirstAfterSkippingEol();
        do {
            if (read != -1) {
                bos.write(read);
            }
        } while (inputStream.available() > 0 && (read=inputStream.read()) != -1);
        final String outputAsString = bos.toString();
        // assume that we've read the prompt; strip it off:
        final Matcher m = promptPattern.matcher(outputAsString);
        int index = 0;
        int startOfPrompt = outputAsString.length();
        int endOfPrompt = outputAsString.length();
        while (m.find(index)) {
            startOfPrompt = m.start();
            endOfPrompt = m.end();
            index = endOfPrompt;
        }
        return outputAsString.substring(0, startOfPrompt).getBytes();
    }

    private int readFirstAfterSkippingEol() throws IOException {
        int read;
        boolean readCarriageReturn = false;
        boolean readLineFeed = false;
        while ((read=inputStream.read()) != -1 &&
                ((char) read == '\n' && !readLineFeed || (char) read == '\r' && !readCarriageReturn)) {
            readLineFeed = readLineFeed || (char) read == '\n';
            readCarriageReturn = readCarriageReturn || (char) read == '\r';
        }
        return read;
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }
    
    /**
     * After having obtained a {@link Channel} from {@link #createSshChannel(String)} or
     * {@link #createRootSshChannel()}, the shell may take some time to start and become responsive. This method will
     * assume the channel passed as {@code sshShellChannel} is not yet {@link Channel#connect() connected}, and obtains
     * the channel's {@link Channel#getOutputStream()}, then connects and uses the streams to send an "echo" command
     * with a generated "stanza" which is then waited for on the channel's {@link Channel#getInputStream() input
     * stream}.
     * @return 
     */
    private Pair<InputStream, OutputStream> waitUntilShellResponse() throws IOException, JSchException {
        final String randomStanza = "Stanza-"+new Random().nextLong();
        final String randomStanzaEchoCommand = "echo \""+randomStanza+"\"\n";
        final InputStream inputStream = channel.getInputStream();
        final OutputStream outputStream = channel.getOutputStream();
        channel.connect(/* timeout in millis */ 5000);
        new Thread(()->{
            try {
                outputStream.write(randomStanzaEchoCommand.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        final byte[] randomStanzaBytesToLookFor = randomStanza.getBytes();
        int i=0;
        int read;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // <= because we'd like to match the line separator at the end
        while (i<=randomStanzaBytesToLookFor.length && (read=inputStream.read())!=-1) {
            bos.write((byte) read);
            if ((i >= randomStanzaBytesToLookFor.length && (char) read != '"')
                    || (i < randomStanzaBytesToLookFor.length && (byte) read == randomStanzaBytesToLookFor[i])) {
                i++;
            } else {
                i = 0;
            }
        }
        if (i != randomStanzaBytesToLookFor.length+1) { // the +1 covers the line separator read
            throw new IllegalStateException("The shell seems unresponsive. You may want to close the channel "+channel);
        } else {
            consumePrompt(inputStream);
        }
        return new Pair<>(inputStream, outputStream);
    }
    
    private void consumePrompt(InputStream inputStream) throws IOException {
        // TODO consider automatic construction of a prompt pattern based on what we see here
        while (inputStream.available() > 0) {
            inputStream.read();
        }
    }
}
