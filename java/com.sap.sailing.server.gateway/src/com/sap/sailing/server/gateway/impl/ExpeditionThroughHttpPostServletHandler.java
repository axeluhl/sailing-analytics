package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.server.gateway.AbstractHttpPostServlet;
import com.sap.sailing.server.gateway.HttpMessageSenderServletRequestHandler;

/**
 * Clients can use this servlet to retrieve Expedition messages through an HTTP connection. The connection remains
 * open until the client closes the request stream. Of course, network errors can occur, so the connection may
 * be closed for other reasons, too. The client may not necessarily become aware of the connection breaking.
 * Therefore, clients can send the string "<ping>" terminated with a newline character through the request stream,
 * and this servlet will respond with the string "<pong>" terminated by a newline on the output stream. The
 * ping/pong messages interleave a stream of Expedition messages but never cut a single expedition message into
 * several pieces. 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ExpeditionThroughHttpPostServletHandler extends HttpMessageSenderServletRequestHandler {
    private final ExpeditionListener listener;
    
    public ExpeditionThroughHttpPostServletHandler(HttpServletResponse resp, AbstractHttpPostServlet owner) throws IOException {
        super(resp, owner);
        listener = new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                synchronized (getWriter()) {
                    send(getWriter(), message.getOriginalMessage().getBytes());
                }
            }
        };
        getService().addExpeditionListener(listener, /* validMessagesOnly */ false);
    }
    
    @Override
    protected void stop() {
        getService().removeExpeditionListener(listener);
        super.stop();
    }
}
