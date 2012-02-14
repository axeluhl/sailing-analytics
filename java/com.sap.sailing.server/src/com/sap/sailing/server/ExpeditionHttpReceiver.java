package com.sap.sailing.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Base64Utils;
import com.sap.sailing.server.impl.AbstractHttpPostServlet;

/**
 * Receives data from a remote servlet, trying to keep the connection open until {@link #stop stopped}. After constructing
 * an instance, clients need to call {@link #connect()} to actually start the process of receiving data through the HTTP
 * connection.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ExpeditionHttpReceiver {
    public interface Receiver {
        /**
         * Called when the receiver has received some bytes from the remote end.
         * @param bytes
         *            0..count-1 hold the bytes received
         * 
         * @return <code>true</code> if the receiver shall terminate and close the connection, <code>false</code>
         *         otherwise
         */
        boolean received(byte[] bytes);
    }

    private static final int BUF_SIZE = 1<<16;
    private static final Logger logger = Logger.getLogger(ExpeditionHttpReceiver.class.getName());
    private final Receiver receiver;
    private final URL url;
    private long timestampOfLastHeartbeatReceived;
    
    /**
     * The input stream can be used to unblock a read in order to terminate the receiver.
     */
    private InputStream inputStream;
    
    private boolean stop = false;
    
    public ExpeditionHttpReceiver(URL url, Receiver receiver) {
        this.receiver = receiver;
        this.url = url;
    }
    
    public void connect() throws IOException, InterruptedException {
        establishConnection(); // performs the actual HTTP connect request, connecting to the servlet
        Thread reader = new Thread(new Runnable() {
            public void run() {
                StringBuilder bos = new StringBuilder();
                while (!stop) {
                    try {
                        Reader reader = new InputStreamReader(inputStream);
                        char[] buf = new char[BUF_SIZE];
                        int read = reader.read(buf);
                        while (!stop && read != -1) {
                            for (int i = 0; i < read; i++) {
                                if (buf[i] == 0) {
                                    // message terminator; one message received
                                    stop = receivedMessage(bos.toString());
                                    bos.delete(0, bos.length());
                                } else {
                                    bos.append(buf[i]);
                                }
                            }
                            if (!stop) {
                                read = reader.read(buf);
                            }
                        }
                        logger.info("Reached EOF");
                        reader.close();
                    } catch (IOException e) {
                        logger.throwing(ExpeditionHttpReceiver.class.getName(), "connect", e);
                    }
                    if (!stop) {
                        logger.info("Reconnecting because not stopped"); 
                        try {
                            establishConnection();
                        } catch (IOException e) {
                            logger.info("Can't re-connect. Giving up.");
                            logger.throwing(ExpeditionHttpReceiver.class.getName(), "connect", e);
                            stop = true;
                        }
                    }
                }
            }

            private boolean receivedMessage(String bos) {
                boolean stopReceiving = false;
                if (bos.equals(AbstractHttpPostServlet.PONG)) {
                    receivedHeartbeatResponse();
                } else {
                    stopReceiving = receiver.received(Base64Utils.fromBase64(bos));
                }
                return stopReceiving;
            }

        }, getClass().getName()+" reader");
        reader.start();
        scheduleTimeoutHandler();
    }

    private void scheduleTimeoutHandler() {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable timeoutChecker = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - timestampOfLastHeartbeatReceived > 5*AbstractHttpPostServlet.HEARTBEAT_TIME_IN_MILLISECONDS) {
                    // TIMEOUT; abort
                    logger.info("Timeout. Didn't receive a heartbeat through my HTTP connection for "+
                        (5*AbstractHttpPostServlet.HEARTBEAT_TIME_IN_MILLISECONDS)+"ms");
                    try {
                        stop();
                    } catch (IOException e) {
                        logger.throwing(ExpeditionHttpReceiver.class.getName(), "scheduleTimeoutHandler", e);
                    }
                } else {
                    scheduler.schedule(this, AbstractHttpPostServlet.HEARTBEAT_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                }
            }
        };
        scheduler.schedule(timeoutChecker, AbstractHttpPostServlet.HEARTBEAT_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    /**
     * connects to the remote servlet using {@link #url} and binds the response input stream to {@link #inputStream}
     */
    private void establishConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        inputStream = connection.getInputStream();
    }
    
    private void receivedHeartbeatResponse() {
        logger.finest("received expedition HTTP heartbeat");
        timestampOfLastHeartbeatReceived = System.currentTimeMillis();
    }

    /**
     * Stops the receiver by closing the writer on the request stream. This will let the server read EOF on the
     * request stream which causes the server to also terminate the sending of data, closing its response stream.
     * This in turn will let the reader return with an EOF.
     * @throws IOException 
     */
    public void stop() throws IOException {
        logger.info("Stopping expedition HTTP receiver");
        stop = true;
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
