package com.sap.sailing.landscape.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.landscape.LandscapeService;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(LandscapeService.class, new LandscapeServiceImpl(), /* properties */ null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
