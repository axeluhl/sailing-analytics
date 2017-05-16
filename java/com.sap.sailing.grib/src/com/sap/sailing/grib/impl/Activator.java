package com.sap.sailing.grib.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.grib.GribWindFieldFactory;

public class Activator implements BundleActivator {
    private GribWindFieldFactory factory;
    
    @Override
    public void start(BundleContext context) throws Exception {
        factory = GribWindFieldFactoryImpl.INSTANCE;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        factory.shutdown();
    }

}
