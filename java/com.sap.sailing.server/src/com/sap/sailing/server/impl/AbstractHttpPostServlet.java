package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.common.Base64Utils;
import com.sap.sailing.server.Servlet;

/**
 * Subclasses can be used to retrieve messages through an HTTP connection. The connection remains open until the client
 * closes the request stream. Of course, network errors can occur, so the connection may be closed for other reasons,
 * too. The client may not necessarily become aware of the connection breaking. Therefore, clients can send the string
 * "<ping>" terminated with a newline character through the request stream, and this servlet will respond with the
 * message "<pong>" on the output stream. The ping/pong messages interleave the message stream but never cut a single
 * message into several parts.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class AbstractHttpPostServlet extends Servlet {
    /**
     * Every so many milliseconds the servlet will send a {@link #PONG} message. This shows clients that the connection
     * is still alive. If after several such intervals the client hasn't received a {@link #PONG} message it seems reasonable
     * to assume the connection has died.
     */
    public static final long HEARTBEAT_TIME_IN_MILLISECONDS = 5000;
    public static final String PONG = "<pong>";
    private static final Logger logger = Logger.getLogger(AbstractHttpPostServlet.class.getName());
    private static final long timeoutInMilliseconds = 60000;
    private long timeInMillisOfLastExpeditionMessageReceived;
    private boolean stop;
    
    private static final long serialVersionUID = 6034769972654796465L;

    protected void stop() {
        stop = true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        HeartbeatHandler heartbeat = new HeartbeatHandler(writer);
        Thread heartbeatHandler = new Thread(heartbeat, getClass().getName()
                + " HeartbeatHandler " + Thread.currentThread().getId());
        startSendingResponse(writer);
        heartbeatHandler.start();
        try {
            Thread.sleep(timeoutInMilliseconds);
            while (!stop && System.currentTimeMillis()-timeInMillisOfLastExpeditionMessageReceived < timeoutInMilliseconds) {
                Thread.sleep(timeInMillisOfLastExpeditionMessageReceived + timeoutInMilliseconds - System.currentTimeMillis());
            }
            if (stop) {
                logger.info(getClass().getName()+" was explicitly stopped, e.g., because client closed connection");
            }
            logger.info("Terminating "+getClass().getName()+" doPost after not receiving anything for "+timeoutInMilliseconds+"ms");
            heartbeat.stop();
            heartbeatHandler.join();
            stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a single message across to the receiving client. To do so, the message is Base64 encoded before being sent
     * as a string. The message is terminated with a 0 byte.
     * @param writer
     * @param bytes
     * @throws IOException
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
     * Used to start sending the response. This may well happen in a separate thread spawned by this method or, e.g., by
     * registering for receiving data and sending it to the <code>writer</code>. To stop the forwarding process,
     * call the {@link #stop} method.
     */
    abstract protected void startSendingResponse(final Writer writer) throws SocketException;
    
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
                    sendString(responseWriter, PONG);
                    Thread.sleep(HEARTBEAT_TIME_IN_MILLISECONDS);
                }
            } catch (Exception e) {
                logger.info("Terminating heartbeat on "+AbstractHttpPostServlet.this.getClass().getName()+
                        " because of exception "+e);
                logger.throwing(getClass().getName(), "run", e);
            }
        }
    }

}
