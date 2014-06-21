package com.sap.sailing.resultimport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.server.RacingEventService;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference<RacingEventService> racingEventServiceReference = context.getServiceReference(RacingEventService.class);
        RacingEventService racingEventService = context.getService(racingEventServiceReference);

        ResultUrlRegistry resultUrlRegistryService = new ResultUrlRegistryImpl(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        context.registerService(ResultUrlRegistry.class, resultUrlRegistryService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
