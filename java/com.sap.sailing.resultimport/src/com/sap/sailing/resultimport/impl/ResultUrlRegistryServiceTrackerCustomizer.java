package com.sap.sailing.resultimport.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;

/**
 * Helps passing on the reference to the usually central instance of a {@link ResultUrlRegistry} service implementation
 * to the services implementing result and competitor import.
 * <p>
 * 
 * Subclass this abstract service tracker customizer and use it when creating a {@link ServiceTracker} for the
 * {@link ResultUrlRegistry} class, then {@link ServiceTracker#open()} that tracker in your result / competitor
 * importer's activator. Then, the {@link #configureScoreCorrectionProvider(ResultUrlRegistry)} and
 * {@link #configureCompetitorProvider(ResultUrlRegistry)} methods will be invoked when the tracker obtains the
 * reference to a/the {@link ResultUrlRegistry} service implementation. If they return a non-{@code null} importer
 * implementation then that implementation will be registered in turn as a {@link ScoreCorrectionProvider} or
 * {@link CompetitorProvider}, respectively.
 * 
 * @author Axel Uhl (d043530)
 *
 */
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
        registerScoreCorrectionProviderService(resultUrlRegistry);
        registerCompetitorImportService(resultUrlRegistry);
        return resultUrlRegistry;
    }

    protected void registerScoreCorrectionProviderService(ResultUrlRegistry resultUrlRegistry) {
        ScoreCorrectionProvider scoreCorrectionProvider = configureScoreCorrectionProvider(resultUrlRegistry);
        if (scoreCorrectionProvider != null) {
            bundleContext.registerService(ScoreCorrectionProvider.class, scoreCorrectionProvider, /* properties */null);
        }
    }

    private void registerCompetitorImportService(ResultUrlRegistry resultUrlRegistry) {
        CompetitorProvider competitorProvider = configureCompetitorProvider(resultUrlRegistry);
        if (competitorProvider != null) {
            bundleContext.registerService(CompetitorProvider.class, competitorProvider, /* properties */null);
        }
    }

    protected abstract ScoreCorrectionProvider configureScoreCorrectionProvider(ResultUrlRegistry resultUrlRegistry);

    /**
     * @return {@code null} by default, so a subclass does not have to make mention of a {@link CompetitorProvider}
     *         implementation
     */
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
