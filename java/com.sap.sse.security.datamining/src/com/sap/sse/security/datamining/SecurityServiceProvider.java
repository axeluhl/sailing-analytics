package com.sap.sse.security.datamining;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.datamining.impl.AbstractDataSourceProvider;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;

public class SecurityServiceProvider extends AbstractDataSourceProvider<SecurityService> {
    
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;

    public SecurityServiceProvider(BundleContext context) {
        super(SecurityService.class);
        securityServiceTracker = createAndOpenRacingEventServiceTracker(context);
    }

    private ServiceTracker<SecurityService, SecurityService> createAndOpenRacingEventServiceTracker(BundleContext context) {
        return FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
    }

    @Override
    public SecurityService getDataSource() {
        return securityServiceTracker.getService();
    }

}
