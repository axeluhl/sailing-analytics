package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sse.MasterDataImportClassLoaderService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private final SwissTimingAdapterFactory swissTimingAdapterFactory;

    public Activator() {
        // there is exactly one instance of the racingEventService in the whole server
        swissTimingAdapterFactory = new SwissTimingAdapterFactoryImpl();
    }
    
    public void start(BundleContext context) throws Exception {
        // register the racing service in the OSGi registry
        context.registerService(SwissTimingAdapterFactory.class.getName(), swissTimingAdapterFactory, null);
        context.registerService(MasterDataImportClassLoaderService.class, new MasterDataImportClassLoaderServiceImpl(), null);
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName());
    }
    
    public void stop(BundleContext context) throws Exception {
    }
}
