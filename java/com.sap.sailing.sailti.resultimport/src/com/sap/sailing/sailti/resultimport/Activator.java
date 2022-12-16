package com.sap.sailing.sailti.resultimport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryServiceTrackerCustomizer;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    public void start(BundleContext bundleContext) throws Exception {
        resultUrlRegistryServiceTracker = ServiceTrackerFactory.createAndOpen(bundleContext, ResultUrlRegistry.class,
                new ResultUrlRegistryServiceTrackerCustomizer(bundleContext) {
                    @Override
                    protected ScoreCorrectionProvider configureScoreCorrectionProvider(ResultUrlRegistry resultUrlRegistry) {
                        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(
                                ParserFactory.INSTANCE, resultUrlRegistry);
                        return service;
                    }
                });
    }

    public void stop(BundleContext bundleContext) throws Exception {
        resultUrlRegistryServiceTracker.close();
    }  
}
