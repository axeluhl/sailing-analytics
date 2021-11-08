package com.sap.sailing.server.gateway.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.gateway.interfaces.SailingServerFactory;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
        final SailingServerFactory sailingServerFactory = new SailingServerFactoryImpl(securityServiceTracker);
        context.registerService(SailingServerFactory.class, sailingServerFactory, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
