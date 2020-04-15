package com.sap.sailing.resultimport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.server.interfaces.RacingEventService;

public class Activator implements BundleActivator {
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context,
                RacingEventService.class.getName(), new RacingEventServiceTrackerCustomizer(context));
        racingEventServiceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        racingEventServiceTracker.close();
    }

    private class RacingEventServiceTrackerCustomizer implements
            ServiceTrackerCustomizer<RacingEventService, RacingEventService> {

        private final BundleContext context;

        public RacingEventServiceTrackerCustomizer(BundleContext context) {
            this.context = context;
        }

        @Override
        public RacingEventService addingService(ServiceReference<RacingEventService> reference) {
            RacingEventService racingEventService = context.getService(reference);

            ResultUrlRegistry resultUrlRegistryService = new ResultUrlRegistryImpl(
                    racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
            context.registerService(ResultUrlRegistry.class, resultUrlRegistryService, null);
            return racingEventService;
        }

        @Override
        public void modifiedService(ServiceReference<RacingEventService> reference, RacingEventService service) {
            
        }

        @Override
        public void removedService(ServiceReference<RacingEventService> reference, RacingEventService service) {            
        }
    }
}
