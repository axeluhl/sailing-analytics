package com.sap.sailing.domain.yellowbrickadapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.base.MasterDataImportClassLoaderService;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapterFactory;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryServiceTrackerCustomizer;
import com.sap.sailing.server.trackfiles.common.GPSFixImporterRegistration;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    static BundleContext getContext() {
        return context;
    }


    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        GPSFixImporterRegistration.register(new YellowBrickGPSFixImporter(), context);
        bundleContext.registerService(YellowBrickTrackingAdapterFactory.class, new YellowBrickTrackingAdapterFactoryImpl(), /* properties */ null);
        bundleContext.registerService(MasterDataImportClassLoaderService.class, new MasterDataImportClassLoaderServiceImpl(), /* properties */ null);
        resultUrlRegistryServiceTracker = new ServiceTracker<>(bundleContext, ResultUrlRegistry.class,
                new ResultUrlRegistryServiceTrackerCustomizer(bundleContext) {
                    @Override
                    protected ScoreCorrectionProvider configureScoreCorrectionProvider(ResultUrlRegistry resultUrlRegistry) {
                        return null;
                    }
                    @Override
                    protected CompetitorProvider configureCompetitorProvider(ResultUrlRegistry resultUrlRegistry) {
                        return new YellowBrickCompetitorProvider(resultUrlRegistry);
                    }
                });
        resultUrlRegistryServiceTracker.open();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
