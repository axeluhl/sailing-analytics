package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private final SwissTimingReplayServiceFactory swissTimingReplayServiceFactory;

    public Activator() {
        // there is exactly one instance of the racingEventService in the whole server
        swissTimingReplayServiceFactory = new SwissTimingReplayServiceFactoryImpl();
    }
    
    public void start(BundleContext context) throws Exception {
        // register the racing service in the OSGi registry
        context.registerService(SwissTimingReplayServiceFactory.class.getName(), swissTimingReplayServiceFactory, null);
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName());
    }
    
    public void stop(BundleContext context) throws Exception {
    }
}
