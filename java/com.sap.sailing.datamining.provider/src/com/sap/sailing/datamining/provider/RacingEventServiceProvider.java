package com.sap.sailing.datamining.provider;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.impl.AbstractDataSourceProvider;

public class RacingEventServiceProvider extends AbstractDataSourceProvider<RacingEventService> {
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public RacingEventServiceProvider(BundleContext context) {
        super(RacingEventService.class);
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
    }

    private ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<>(context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }

    @Override
    public RacingEventService getDataSource() {
        return racingEventServiceTracker.getService();
    }

}
