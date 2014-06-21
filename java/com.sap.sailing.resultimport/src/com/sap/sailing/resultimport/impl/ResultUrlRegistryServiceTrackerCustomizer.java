package com.sap.sailing.resultimport.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;

public abstract class ResultUrlRegistryServiceTrackerCustomizer implements
        ServiceTrackerCustomizer<ResultUrlRegistry, ResultUrlRegistry> {

    private final BundleContext bundleContext;

    public ResultUrlRegistryServiceTrackerCustomizer(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ResultUrlRegistry addingService(ServiceReference<ResultUrlRegistry> reference) {
        ServiceReference<ResultUrlRegistry> serviceRefUrlRegistry = bundleContext
                .getServiceReference(ResultUrlRegistry.class);
        ResultUrlRegistry resultUrlRegistry = bundleContext.getService(serviceRefUrlRegistry);

        ScoreCorrectionProvider scoreCorrectionProvider = configureScoreCorrectionProvider(resultUrlRegistry);
        bundleContext.registerService(ScoreCorrectionProvider.class, scoreCorrectionProvider, /* properties */null);
        return resultUrlRegistry;
    }

    protected abstract ScoreCorrectionProvider configureScoreCorrectionProvider(ResultUrlRegistry resultUrlRegistry);

    @Override
    public void modifiedService(ServiceReference<ResultUrlRegistry> reference, ResultUrlRegistry service) {
    }

    @Override
    public void removedService(ServiceReference<ResultUrlRegistry> reference, ResultUrlRegistry service) {
    }

}
