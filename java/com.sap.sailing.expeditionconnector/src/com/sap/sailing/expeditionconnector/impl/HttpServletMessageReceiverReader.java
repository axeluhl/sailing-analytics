package com.sap.sailing.expeditionconnector.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.HttpMessageSenderServletConstants;
import com.sap.sailing.expeditionconnector.impl.HttpServletMessageReceiver.Receiver;
import com.sap.sse.common.Base64Utils;

public class HttpServletMessageReceiverReader implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpServletMessageReceiverReader.class.getName());

    private static final int BUF_SIZE = 1<<16;
    private boolean stopped;
    private Reader reader;

    private final URL url;

    private final Receiver receiver;

    private final HttpServletMessageReceiver owner;
    
    public HttpServletMessageReceiverReader(URL url, Receiver receiver, HttpServletMessageReceiver owner) throws IOException {
        this.url = url;
        this.receiver = receiver;
        this.owner = owner;
        establishConnection();
    }
    
    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
        // close reader in separate thread; it may block
        new Thread("ExpeditionHttpReceiverReader closer") {
            public void run() {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.info("Exception trying to close reader");
                }
            }
        }.start();
    }

    @Override
    public void run() {
        StringBuilder bos = new StringBuilder();
        while (!isStopped()) {
            try {
                char[] buf = new char[BUF_SIZE];
                int read = reader.read(buf);
                while (!isStopped() && read != -1) {
                    for (int i = 0; i < read; i++) {
                        if (buf[i] == 0) {
                            // message terminator; one message received
                            if (receivedMessage(bos.toString())) {
                                stop();
                            }
                            bos.delete(0, bos.length());
                        } else {
                            bos.append(buf[i]);
                        }
                    }
                    if (!isStopped()) {
                        read = reader.read(buf);
                    }
                }
                logger.info("Reached EOF");
                reader.close();
            } catch (IOException e) {
                logger.throwing(HttpServletMessageReceiver.class.getName(), "connect", e);
            }
            if (!isStopped()) {
                logger.info("Reconnecting because not stopped");
                try {
                    establishConnection();
                } catch (IOException e) {
                    logger.info("Can't re-connect. Giving up.");
                    logger.throwing(HttpServletMessageReceiver.class.getName(), "connect", e);
                    stop();
                }
            }
        }
    }

    private boolean receivedMessage(String bos) {
        boolean stopReceiving = false;
        if (bos.equals(HttpMessageSenderServletConstants.PONG)) {
            owner.receivedHeartbeatResponse();
        } else {
            stopReceiving = receiver.received(Base64Utils.fromBase64(bos));
        }
        return stopReceiving;
    }

    /**
     * connects to the remote servlet using {@link #url} and binds the response input stream to {@link #reader}
     */
    private void establishConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        reader = new InputStreamReader(connection.getInputStream());
    }
}
