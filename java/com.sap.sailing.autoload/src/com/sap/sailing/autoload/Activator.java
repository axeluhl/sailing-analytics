package com.sap.sailing.autoload;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.RacingEventService;


public class Activator implements BundleActivator {
    Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext context) throws Exception {
        RacingEventService racingEventService = createAndOpenRacingEventServiceTracker(context).getService();
        new TracTrac(context, racingEventService);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
    
    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }
}
