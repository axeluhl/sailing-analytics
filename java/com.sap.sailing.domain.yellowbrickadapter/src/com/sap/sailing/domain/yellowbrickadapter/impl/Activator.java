package com.sap.sailing.domain.yellowbrickadapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapterFactory;
import com.sap.sailing.server.trackfiles.common.GPSFixImporterRegistration;
import com.sap.sse.MasterDataImportClassLoaderService;

public class Activator implements BundleActivator {
    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        GPSFixImporterRegistration.register(new YellowBrickGPSFixImporter(), context);
        bundleContext.registerService(YellowBrickTrackingAdapterFactory.class, new YellowBrickTrackingAdapterFactoryImpl(), /* properties */ null);
        bundleContext.registerService(MasterDataImportClassLoaderService.class, new MasterDataImportClassLoaderServiceImpl(), /* properties */ null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
