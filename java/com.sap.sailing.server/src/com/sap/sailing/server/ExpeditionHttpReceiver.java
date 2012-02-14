package com.sap.sailing.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class ExpeditionHttpReceiver implements Runnable {
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
    private static final long HEARTBEAT_TIME_IN_MILLISECONDS = 1000;
    private static final Logger logger = Logger.getLogger(ExpeditionHttpReceiver.class.getName());
    private final Receiver receiver;
    private final URL url;
    private PrintWriter requestWriter;
    private boolean stop = false;
    
    public ExpeditionHttpReceiver(URL url, Receiver receiver) {
        this.receiver = receiver;
        this.url = url;
    }
    
    public void connect() throws IOException, InterruptedException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setChunkedStreamingMode(/* chunklen */ BUF_SIZE);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.connect();
        requestWriter = new PrintWriter(connection.getOutputStream());
        requestWriter.println(AbstractHttpPostServlet.PING); // ensure the stream writes through to the other end
        requestWriter.flush();
        final InputStream inputStream = connection.getInputStream();
        new Thread(this, getClass().getName()+" Heartbeat").start();
        Thread reader = new Thread(new Runnable() {
            public void run() {
                StringBuilder bos = new StringBuilder();
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
                    reader.close();
                } catch (IOException e) {
                    logger.throwing(ExpeditionHttpReceiver.class.getName(), "connect", e);
                }
            }

            protected boolean receivedMessage(String bos) {
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
        reader.join();
        stop = true; // make sure the heartbeat thread stops
        if (requestWriter != null) {
            requestWriter.close();
        }
    }
    
    private void receivedHeartbeatResponse() {
        // TODO do we want to keep track of received heartbeat responses?
    }

    /**
     * Stops the receiver by closing the writer on the request stream. This will let the server read EOF on the
     * request stream which causes the server to also terminate the sending of data, closing its response stream.
     * This in turn will let the reader return with an EOF.
     */
    public void stop() {
        stop = true;
        if (requestWriter != null) {
            requestWriter.close();
        }
    }
    
    /**
     * Implements the heartbeat by sending a "&lt;ping&gt;" message every {@link #HEARTBEAT_TIME_IN_MILLISECONDS} milliseconds.
     * If that fails with an exception, the {@link #stop} method is called.
     */
    @Override
    public void run() {
        try {
            while (!stop && requestWriter != null) {
                Thread.sleep(HEARTBEAT_TIME_IN_MILLISECONDS);
                if (requestWriter != null) {
                    synchronized (requestWriter) {
                        requestWriter.println(AbstractHttpPostServlet.PING);
                        requestWriter.flush();
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.throwing(ExpeditionHttpReceiver.class.getName(), "run", e);
        }
    }
}
