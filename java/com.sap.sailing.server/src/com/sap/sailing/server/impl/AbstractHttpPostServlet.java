package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
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
    public static final String PING = "<ping>";
    public static final String PONG = "<pong>";
    private static final Logger logger = Logger.getLogger(AbstractHttpPostServlet.class.getName());
    private static final long timeoutInMilliseconds = 60000;
    private long timeInMillisOfLastExpeditionMessageReceived;
    
    private static final long serialVersionUID = 6034769972654796465L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        final ServletInputStream requestInputStream = req.getInputStream();
        Thread heartbeatHandler = new Thread(new HeartbeatHandler(requestInputStream, writer), getClass().getName()
                + " HeartbeatHandler " + Thread.currentThread().getId());
        startSendingResponse(writer, new Runnable() {
            public void run() {
                try {
                    requestInputStream.close();
                } catch (IOException e) {
                    logger.throwing(ExpeditionThroughHttpPostServlet.class.getName(), "run", e);
                }
            }
        });
        heartbeatHandler.start();
        try {
            Thread.sleep(timeoutInMilliseconds);
            while (System.currentTimeMillis()-timeInMillisOfLastExpeditionMessageReceived < timeoutInMilliseconds) {
                Thread.sleep(timeInMillisOfLastExpeditionMessageReceived + timeoutInMilliseconds - System.currentTimeMillis());
            }
            logger.info("Terminating "+getClass().getName()+" doPost after not receiving anything for "+timeoutInMilliseconds+"ms");
            heartbeatHandler.join();
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
    protected void send(Writer writer, byte[] message) throws IOException {
        timeInMillisOfLastExpeditionMessageReceived = System.currentTimeMillis();
        synchronized (writer) {
            writer.write(Base64Utils.toBase64(message));
            writer.write(0); // terminate message with 0 character
            writer.flush();
        }
    }
    
    /**
     * Used to start sending the response. This may well happen in a separate thread spawned by this method or, e.g., by
     * registering for receiving data and sending it to the <code>writer</code>. To stop the forwarding process,
     * call the <code>runToStop</code> object's {@link Runnable#run()} method.
     */
    abstract protected void startSendingResponse(final Writer writer, final Runnable runToStop) throws SocketException;
    
    /**
     * Reads from the request input stream. If a "<ping>\n" message comes along, a "<pong>\n" message will be sent back.
     * This will allow a client to regularly check live-ness of the connection and reconnect if needed.
     * 
     * @author Axel Uhl (D043530)
     */
    private class HeartbeatHandler implements Runnable {
        private final InputStream requestInputStream;
        private final PrintWriter responseWriter;
        
        public HeartbeatHandler(InputStream requestInputStream, PrintWriter responseWriter) {
            super();
            this.requestInputStream = requestInputStream;
            this.responseWriter = responseWriter;
        }

        @Override
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(requestInputStream));
            try {
                String line = br.readLine();
                while (line != null) {
                    if (line.equals(PING)) {
                        synchronized (responseWriter) {
                            send(responseWriter, PONG.getBytes());
                        }
                    }
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
