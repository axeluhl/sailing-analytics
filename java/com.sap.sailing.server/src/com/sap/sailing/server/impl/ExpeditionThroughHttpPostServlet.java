package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.util.logging.Logger;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.server.RacingEventService;

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
public class ExpeditionThroughHttpPostServlet extends AbstractHttpPostServlet {
    private static final long serialVersionUID = 4409173886816756920L;
    private static final Logger logger = Logger.getLogger(ExpeditionThroughHttpPostServlet.class.getName());
    
    /**
     * Used to start sending the response. This may well happen in a separate thread spawned by this method or, e.g., by
     * registering for receiving data and sending it to the <code>writer</code>. To stop the forwarding process,
     * call the <code>runToStop</code> object's {@link Runnable#run()} method.
     */
    protected void startSendingResponse(final Writer writer, final Runnable runToStop) throws SocketException {
        RacingEventService service = getService();
        service.addExpeditionListener(new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                synchronized (writer) {
                    try {
                        send(writer, message.getOriginalMessage().getBytes());
                    } catch (IOException e) {
                        logger.throwing(ExpeditionThroughHttpPostServlet.class.getName(), "received", e);
                        runToStop.run();
                    }
                }
            }
        }, /* validMessagesOnly */ false);
    }
}
