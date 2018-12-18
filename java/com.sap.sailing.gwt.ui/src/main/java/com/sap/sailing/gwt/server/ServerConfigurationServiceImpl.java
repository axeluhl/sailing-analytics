package com.sap.sailing.gwt.server;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.client.ServerConfigurationService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.gwt.dispatch.servlets.ProxiedRemoteServiceServlet;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * The server side implementation of the RPC service.
 */
public class ServerConfigurationServiceImpl extends ProxiedRemoteServiceServlet implements ServerConfigurationService {
    private static final long serialVersionUID = 2571063542941630865L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public ServerConfigurationServiceImpl() {
        BundleContext context = Activator.getDefault();
        
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); 
    }

    @Override
    public boolean isStandaloneServer() {
        return getService().getSailingServerConfiguration().isStandaloneServer();
    }
}
