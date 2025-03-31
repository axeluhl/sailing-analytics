package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.expeditionconnector.ExpeditionTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
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
    private static final Logger logger = Logger.getLogger(ExpeditionThroughHttpPostServletHandler.class.getName());
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
        addExpeditionListener(listener, /* validMessagesOnly */ false);
    }
    
    private void addExpeditionListener(ExpeditionListener listener, boolean validMessagesOnly) throws SocketException {
        UDPExpeditionReceiver receiver = createExpeditionTrackerFactory(getContext()).getService().getOrCreateWindReceiverOnDefaultPort();
        receiver.addListener(listener, validMessagesOnly);
    }
    
    private ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory> createExpeditionTrackerFactory(BundleContext context) {
        ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory> result = new ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory>(
                getContext(), ExpeditionTrackerFactory.class.getName(), null);
        result.open();
        return result;
    }

    @Override
    protected void stop() {
        UDPExpeditionReceiver receiver;
        try {
            ExpeditionTrackerFactory windTrackerFactory = createExpeditionTrackerFactory(getContext()).getService();
            receiver = windTrackerFactory.getOrCreateWindReceiverOnDefaultPort();
            receiver.removeListener(listener);
        } catch (SocketException e) {
            logger.info("Failed to remove expedition listener " + listener
                    + "; exception while trying to retrieve wind receiver: " + e.getMessage());
        }
        super.stop();
    }
}
