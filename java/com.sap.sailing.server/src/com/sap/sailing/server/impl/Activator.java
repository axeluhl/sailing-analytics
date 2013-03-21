package com.sap.sailing.server.impl;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.server.RacingEventService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static ExtenderBundleTracker extenderBundleTracker;

    private final RacingEventService racingEventService;

    public Activator() {
        // there is exactly one instance of the racingEventService in the whole server
        racingEventService = new RacingEventServiceImpl();
    }
    
    public void start(BundleContext context) throws Exception {
        extenderBundleTracker = new ExtenderBundleTracker(context);
        extenderBundleTracker.open();

        // register the racing service in the OSGi registry
        context.registerService(RacingEventService.class.getName(), racingEventService, null);

        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName()+". Character encoding: "+
                Charset.defaultCharset());
    }
    
    public void stop(BundleContext context) throws Exception {
        if(extenderBundleTracker != null) {
            extenderBundleTracker.close();
        }

        // stop the tracking of the wind and all races
        for (Triple<Regatta, RaceDefinition, String> windTracker : racingEventService.getWindTrackedRaces()) {
            racingEventService.stopTrackingWind(windTracker.getA(), windTracker.getB());
        }
        for (Regatta regatta : racingEventService.getAllRegattas()) {
            racingEventService.stopTracking(regatta);
        }
    }
}
