package com.sap.sailing.expeditionconnector.impl;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.HttpMessageSenderServletConstants;
import com.sap.sse.util.impl.ThreadFactoryWithPriority;


/**
 * Receives data from a remote servlet whose implementation is expected to subclass {@link AbstractHttpPostServlet},
 * trying to keep the connection open until {@link #stop() stopped}. Will try to re-establish a broken connection until
 * {@link #stop()} is called. After constructing an instance, clients need to call {@link #connect()} to actually start
 * the process of receiving data through the HTTP connection. Waiters on this object will be notified whenever the
 * {@link #isStopped()} result has changed.
 * <p>
 * 
 * Clients pass a {@link Receiver} object to the constructor. That object's {@link Receiver#received(byte[])} method
 * will be called upon each message received.<p>
 * 
 * Uses a heart beat protocol with the servlet to detect a broken connection. The server is expected to send a heart
 * beat signal every {@link AbstractHttpPostServlet#HEARTBEAT_TIME_IN_MILLISECONDS} milliseconds. This client accepts a
 * five-fold delay while receiving this heart beat. If the heart beat is not received in time,
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class HttpServletMessageReceiver {
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

    private static final Logger logger = Logger.getLogger(HttpServletMessageReceiver.class.getName());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryWithPriority(Thread.NORM_PRIORITY, /* daemon */ true));
    private final Receiver receiver;
    private long timestampOfLastHeartbeatReceived;
    private final URL url;
    
    private boolean stop = false;
    
    /**
     * @param url the URL of a {@link AbstractHttpPostServlet} servlet from which to receive messages
     * @param receiver will have its {@link Receiver#received(byte[])} method called for each message received
     */
    public HttpServletMessageReceiver(URL url, Receiver receiver) {
        this.receiver = receiver;
        this.url = url;
    }
    
    public void connect() throws IOException, InterruptedException {
        HttpServletMessageReceiverReader readerRunnable = new HttpServletMessageReceiverReader(url, receiver, this);
        Thread readerThread = new Thread(readerRunnable, getClass().getName()+" reader");
        readerThread.start();
        scheduleTimeoutHandler(readerRunnable);
    }

    private void scheduleTimeoutHandler(final HttpServletMessageReceiverReader readerRunnable) {
        Runnable timeoutChecker = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - timestampOfLastHeartbeatReceived > 5*HttpMessageSenderServletConstants.HEARTBEAT_TIME_IN_MILLISECONDS) {
                    // TIMEOUT; abort
                    logger.info("Timeout. Didn't receive a heartbeat through my HTTP connection for "+
                        (5*HttpMessageSenderServletConstants.HEARTBEAT_TIME_IN_MILLISECONDS)+"ms");
                    readerRunnable.stop();
                    boolean successfullyConnected = false;
                    while (!isStopped() && !successfullyConnected) {
                        try {
                            logger.info("Trying to re-connect to "+url);
                            connect(); // open another connection using another ExpeditionHttpReceiverReader
                            logger.info("Successfully re-connected to "+url);
                            successfullyConnected = true;
                        } catch (IOException | InterruptedException e) {
                            logger.throwing(HttpServletMessageReceiver.class.getName(), "run", e);
                            logger.info("Error "+e.getMessage()+" trying to re-connect; will keep trying in a few seconds");
                            try {
                                Thread.sleep(3);
                            } catch (InterruptedException e1) {
                                logger.throwing(HttpServletMessageReceiver.class.getName(), "can't even sleep", e1);
                            }
                        }
                    }
                } else {
                    scheduler.schedule(this, HttpMessageSenderServletConstants.HEARTBEAT_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                }
            }
        };
        scheduler.schedule(timeoutChecker, HttpMessageSenderServletConstants.HEARTBEAT_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the receiver by closing the writer on the request stream. This will let the server read EOF on the
     * request stream which causes the server to also terminate the sending of data, closing its response stream.
     * This in turn will let the reader return with an EOF.
     * @throws IOException 
     */
    public void stop() throws IOException {
        logger.info("Stopping expedition HTTP receiver");
        setStop(true);
    }
    
    /**
     * Tells if this receiver is in <code>stopped</code> state. This means that it won't receive messages anymore.
     * It may still be that for a hanging socket there is still a thread blocked in a <code>read</code> call, but
     * when that read call returns or abort, the thread will end, too.<p>
     * 
     * When this receiver's stopped state changes, all waiters on this object are notified.
     */
    public boolean isStopped() {
        return stop;
    }

    private void setStop(boolean stop) {
        if (stop != this.stop) {
            synchronized (this) {
                this.stop = stop;
                notifyAll();
            }
        }
    }

    public void receivedHeartbeatResponse() {
        logger.finest("received expedition HTTP heartbeat");
        timestampOfLastHeartbeatReceived = System.currentTimeMillis();
    }
}
