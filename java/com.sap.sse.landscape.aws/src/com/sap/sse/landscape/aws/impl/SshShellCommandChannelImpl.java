package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.SshShellCommandChannel;


public class SshShellCommandChannelImpl implements SshShellCommandChannel {
    private static final Logger logger = Logger.getLogger(SshShellCommandChannelImpl.class.getName());
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
    public SshShellCommandChannelImpl(Channel channel) throws IOException, JSchException, InterruptedException {
        this(channel, DEFAULT_PROMPT_PATTERN);
    }
    
    public SshShellCommandChannelImpl(Channel channel, Pattern promptPattern) throws IOException, JSchException, InterruptedException {
        super();
        this.channel = channel;
        this.promptPattern = promptPattern;
        final Pair<InputStream, OutputStream> streams = waitUntilShellResponse();
        this.inputStream = streams.getA();
        this.outputStream = streams.getB();
    }

    @Override
    public byte[] sendCommandLineSynchronously(String commandLine) throws IOException, InterruptedException, JSchException {
        new Thread(()->{
            try {
                outputStream.write((commandLine+"\n").getBytes());
                outputStream.flush();
                logger.info("Sent command \""+commandLine+"\" to "+getHost());
            } catch (IOException | JSchException e) {
                throw new RuntimeException(e);
            }
        }).start();
        waitForAvailableInput(Duration.ONE_SECOND);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ByteArrayOutputStream echo = new ByteArrayOutputStream();
        // assume the command line got echoed; read it because we don't want it as part of the output
        for (int i=0; i<commandLine.length() && inputStream.available() > 0; i++) {
            echo.write(inputStream.read());
        }
        logger.info("Read echoed command \""+echo+"\" from host "+getHost());
        int read = readFirstAfterSkippingEol();
        do {
            if (read != -1) {
                bos.write(read);
            }
        } while (inputStream.available() > 0 && (read=inputStream.read()) != -1);
        final String outputAsString = bos.toString();
        logger.info("Read command output \""+outputAsString+"\" from host "+getHost());
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
        String outputWithoutTrailingPrompt = outputAsString.substring(0, startOfPrompt);
        logger.info("Output after stripping off trailing prompt: \""+
                outputWithoutTrailingPrompt+"\"");
        return outputWithoutTrailingPrompt.getBytes();
    }

    private String getHost() throws JSchException {
        return channel.getSession().getHost();
    }

    /**
     * Waits in 10ms increments up to {@code timeout} for input on the {@link #inputStream}
     * to become available.
     */
    private void waitForAvailableInput(Duration timeout) throws IOException, InterruptedException {
        // wait a bit for echoed command to become available:
        final Duration interval = Duration.ONE_MILLISECOND.times(10);
        double runs = timeout.divide(interval);
        while (inputStream.available() == 0 && --runs > 0) {
            Thread.sleep(interval.asMillis());
        }
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
     */
    private Pair<InputStream, OutputStream> waitUntilShellResponse() throws IOException, JSchException, InterruptedException {
        logger.info("Waiting for SSH shell response from host "+getHost());
        final String randomStanza = "Stanza-"+new Random().nextLong();
        final String randomStanzaEchoCommand = "echo \""+randomStanza+"\"\n";
        final InputStream inputStream = channel.getInputStream();
        final OutputStream outputStream = channel.getOutputStream();
        channel.connect(/* timeout in millis */ 5000);
        new Thread(()->{
            try {
                outputStream.write(randomStanzaEchoCommand.getBytes());
                outputStream.flush();
                logger.info("Sent random stanza echo command: \""+randomStanzaEchoCommand+"\"");
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
            logger.info("Found stanza "+randomStanza);
            consumePrompt(inputStream);
        }
        return new Pair<>(inputStream, outputStream);
    }
    
    private void consumePrompt(InputStream inputStream) throws IOException, InterruptedException {
        // TODO consider automatic construction of a prompt pattern based on what we see here
        final Duration timeout = Duration.ONE_SECOND;
        final TimePoint start = TimePoint.now();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (!promptPattern.matcher(bos.toString()).find() && start.plus(timeout).after(TimePoint.now())) {
            while (inputStream.available() == 0 && start.plus(timeout).after(TimePoint.now())) {
                Thread.sleep(10);
            }
            if (inputStream.available() > 0) {
                bos.write(inputStream.read());
            }
        }
        logger.info("Consumed as prompt: "+bos);
    }
}
