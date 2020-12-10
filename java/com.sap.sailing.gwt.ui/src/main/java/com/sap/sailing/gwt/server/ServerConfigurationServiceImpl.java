package com.sap.sailing.gwt.server;

import org.osgi.framework.BundleContext;

import com.sap.sailing.gwt.ui.client.ServerConfigurationService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.gwt.server.ProxiedRemoteServiceServlet;
import com.sap.sse.replication.FullyInitializedReplicableTracker;

/**
 * The server side implementation of the RPC service.
 */
public class ServerConfigurationServiceImpl extends ProxiedRemoteServiceServlet implements ServerConfigurationService {
    private static final long serialVersionUID = 2571063542941630865L;

    private final FullyInitializedReplicableTracker<RacingEventService> racingEventServiceTracker;

    public ServerConfigurationServiceImpl() {
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        try {
            return racingEventServiceTracker.getInitializedService(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } 
    }

    @Override
    public boolean isStandaloneServer() {
        return getService().getSailingServerConfiguration().isStandaloneServer();
    }
}
