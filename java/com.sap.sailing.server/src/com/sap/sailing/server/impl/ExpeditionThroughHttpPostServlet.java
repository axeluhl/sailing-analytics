package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.Servlet;

public class ExpeditionThroughHttpPostServlet extends Servlet {
    private static final long timeoutInMilliseconds = 60000;
    private long timeInMillisOfLastExpeditionMessageReceived;
    
    private static final long serialVersionUID = 6034769972654796465L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RacingEventService service = getService();
        final PrintWriter writer = resp.getWriter();
        Thread pingPongHandler = new Thread(new PingPongHandler(req.getInputStream(), writer), getClass().getName()
                + " PingPongHandler " + Thread.currentThread().getId());
        service.addExpeditionListener(new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                timeInMillisOfLastExpeditionMessageReceived = System.currentTimeMillis();
                synchronized (writer) {
                    writer.println(message.getOriginalMessage());
                    writer.flush();
                }
            }
        }, /* validMessagesOnly */ false);
        pingPongHandler.start();
        try {
            Thread.sleep(timeoutInMilliseconds);
            while (System.currentTimeMillis()-timeInMillisOfLastExpeditionMessageReceived < timeoutInMilliseconds) {
                Thread.sleep(timeInMillisOfLastExpeditionMessageReceived + timeoutInMilliseconds - System.currentTimeMillis());
            }
            pingPongHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Reads from the request input stream. If a "<ping>\n" message comes along, a "<pong>\n" message will be sent back.
     * This will allow a client to regularly check live-ness of the connection and reconnect if needed.
     * 
     * @author Axel Uhl (D043530)
     */
    private class PingPongHandler implements Runnable {
        private final InputStream requestInputStream;
        private final PrintWriter responseWriter;
        
        public PingPongHandler(InputStream requestInputStream, PrintWriter responseWriter) {
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
                    if (line.equals("<ping>")) {
                        synchronized (responseWriter) {
                            responseWriter.println("<pong>");
                            responseWriter.flush();
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
