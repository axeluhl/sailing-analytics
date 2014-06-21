package com.sap.sailing.resultimport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.server.RacingEventService;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        BundleContext serverContext = FrameworkUtil.getBundle(RacingEventService.class).getBundleContext();
        ServiceReference<RacingEventService> racingEventServiceReference = serverContext
                .getServiceReference(RacingEventService.class);
        RacingEventService racingEventService = serverContext.getService(racingEventServiceReference);

        ResultUrlRegistry resultUrlRegistryService = new ResultUrlRegistryImpl(
                racingEventService.getMongoObjectFactory(), racingEventService.getDomainObjectFactory());
        context.registerService(ResultUrlRegistry.class, resultUrlRegistryService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    public static ResultUrlRegistry getResultUrlRegistryService(BundleContext bundleContext) {
        BundleContext resultImportBundleContext = FrameworkUtil.getBundle(ResultUrlRegistry.class).getBundleContext();
        ServiceReference<ResultUrlRegistry> serviceReference = resultImportBundleContext
                .getServiceReference(ResultUrlRegistry.class);
        ResultUrlRegistry resultUrlRegistry = bundleContext.getService(serviceReference);
        return resultUrlRegistry;
    }

}
