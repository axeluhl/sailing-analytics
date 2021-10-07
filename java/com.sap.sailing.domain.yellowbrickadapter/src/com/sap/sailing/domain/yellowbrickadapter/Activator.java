package com.sap.sailing.domain.yellowbrickadapter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.server.trackfiles.common.GPSFixImporterRegistration;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        GPSFixImporterRegistration.register(new YellowBrickGPSFixImporter(), context);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
