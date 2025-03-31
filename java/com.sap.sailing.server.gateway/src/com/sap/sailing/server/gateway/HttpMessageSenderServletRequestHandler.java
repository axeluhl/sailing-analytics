package com.sap.sailing.server.gateway;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.HttpMessageSenderServletConstants;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Base64Utils;

public abstract class HttpMessageSenderServletRequestHandler {
    private static final Logger logger = Logger.getLogger(AbstractHttpPostServlet.class.getName());

    /**
     * Every so many milliseconds the servlet will send a {@link #PONG} message. This shows clients that the connection
     * is still alive. If after several such intervals the client hasn't received a {@link #PONG} message it seems reasonable
     * to assume the connection has died.
     */
    private static final long timeoutInMilliseconds = 60000;
    private long timeInMillisOfLastExpeditionMessageReceived;
    private boolean stop;
    
    private final PrintWriter writer;
    private final AbstractHttpPostServlet owner;
    
    public HttpMessageSenderServletRequestHandler(HttpServletResponse resp, AbstractHttpPostServlet owner) throws IOException {
        this.owner = owner;
        this.writer = resp.getWriter();
    }
    
    protected BundleContext getContext() {
        return owner.getContext();
    }
    
    protected RacingEventService getService() {
        return owner.getService();
    }
    
    /**
     * Obtains the writer through which the server can push data to the client.
     */
    protected PrintWriter getWriter() {
        return writer;
    }

    protected synchronized void stop() {
        stop = true;
        notifyAll();
    }
    
    private boolean isStopped() {
        return stop;
    }

    protected void handleRequest() throws ServletException, IOException {
        stop = false;
        HeartbeatHandler heartbeat = new HeartbeatHandler(writer);
        Thread heartbeatHandler = new Thread(heartbeat, getClass().getName()
                + " HeartbeatHandler " + Thread.currentThread().getId());
        heartbeatHandler.start();
        try {
            synchronized (this) {
                timeInMillisOfLastExpeditionMessageReceived = System.currentTimeMillis(); // initialize timeout counter
                while (!isStopped() && System.currentTimeMillis()-timeInMillisOfLastExpeditionMessageReceived < timeoutInMilliseconds) {
                    wait(timeInMillisOfLastExpeditionMessageReceived + timeoutInMilliseconds - System.currentTimeMillis());
                }
            }
            if (isStopped()) {
                logger.info(getClass().getName()+" was explicitly stopped, e.g., because client closed connection");
            }
            logger.info("Terminating "+getClass().getName()+" doPost after not receiving anything for "+timeoutInMilliseconds+"ms");
            heartbeat.stop();
            heartbeatHandler.join();
            if (!isStopped()) {
                stop();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a single message across to the receiving client. To do so, the message is Base64 encoded before being sent
     * as a string. The message is terminated with a 0 byte.
     */
    protected void send(Writer writer, byte[] message)  {
        String bytes = Base64Utils.toBase64(message);
        sendString(writer, bytes);
    }

    protected void sendString(Writer writer, String s) {
        timeInMillisOfLastExpeditionMessageReceived = System.currentTimeMillis();
        try {
            synchronized (writer) {
                writer.write(s);
                writer.write(0); // terminate message with 0 character
                writer.flush();
            }
        } catch (IOException e) {
            stop();
        }
    }
    
    /**
     * Every {@link #HEARTBEAT_TIME_IN_MILLISECONDS} milliseconds, a "<pong>\n" message will be sent to {@link #responseWriter}.
     * This will allow a client to regularly check live-ness of the connection and reconnect if needed. If {@link #stop} is
     * called on this object, the heartbeat sending is stopped and the {@link #run} method returns.
     * 
     * @author Axel Uhl (D043530)
     */
    private class HeartbeatHandler implements Runnable {
        private boolean stop = false;
        private final PrintWriter responseWriter;
        
        public HeartbeatHandler(PrintWriter responseWriter) {
            super();
            this.responseWriter = responseWriter;
        }
        
        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            try {
                while (!stop) {
                    sendString(responseWriter, HttpMessageSenderServletConstants.PONG);
                    Thread.sleep(HttpMessageSenderServletConstants.HEARTBEAT_TIME_IN_MILLISECONDS);
                }
            } catch (Exception e) {
                logger.info("Terminating heartbeat on "+HttpMessageSenderServletRequestHandler.this.getClass().getName()+
                        " because of exception "+e);
                logger.log(Level.SEVERE, "run", e);
            }
            HttpMessageSenderServletRequestHandler.this.stop();
        }
    }
}
