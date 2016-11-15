package com.sap.sailing.resultimport.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sailing.competitorimport.CompetitorProvider;
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

        registerCompetitorImportService(resultUrlRegistry);
        return resultUrlRegistry;
    }

    private void registerCompetitorImportService(ResultUrlRegistry resultUrlRegistry) {
        CompetitorProvider competitorProvider = configureCompetitorProvider(resultUrlRegistry);
        if (competitorProvider != null) {
            bundleContext.registerService(CompetitorProvider.class, competitorProvider, /* properties */null);
        }
    }

    protected abstract ScoreCorrectionProvider configureScoreCorrectionProvider(ResultUrlRegistry resultUrlRegistry);

    // return null by default to not to create create dependency on competitor import in freg and yachtscoring since we haven't
    // had implementation for importing competitors for this bundles yet
    protected  CompetitorProvider configureCompetitorProvider(ResultUrlRegistry resultUrlRegistry) {
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ResultUrlRegistry> reference, ResultUrlRegistry service) {
    }

    @Override
    public void removedService(ServiceReference<ResultUrlRegistry> reference, ResultUrlRegistry service) {
    }

}
