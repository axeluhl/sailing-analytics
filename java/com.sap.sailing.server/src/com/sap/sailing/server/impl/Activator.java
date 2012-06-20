package com.sap.sailing.server.impl;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.server.RacingEventService;

public class Activator implements BundleActivator, ServiceListener {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static BundleContext fContext;

    private static ExtenderBundleTracker extenderBundleTracker;
    
    public static BundleContext getDefault() {
        return fContext;
    }

    public void start(BundleContext context) throws Exception {
        fContext = context;
        
        extenderBundleTracker = new ExtenderBundleTracker(context);
        extenderBundleTracker.open();
        RacingEventService service = new RacingEventServiceImpl();

        // register the racing service
        context.registerService(RacingEventService.class.getName(), service, null);

        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName()+". Character encoding: "+
                Charset.defaultCharset());
    }
    
    public void stop(BundleContext context) throws Exception {
        fContext = null;
        
        if(extenderBundleTracker != null) {
            extenderBundleTracker.open();
        }

        ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        
        racingEventServiceTracker.open();
        // grab the service
        RacingEventService service = (RacingEventService) racingEventServiceTracker.getService();
        for (Triple<Regatta, RaceDefinition, String> windTracker : service.getWindTrackedRaces()) {
            service.stopTrackingWind(windTracker.getA(), windTracker.getB());
        }
        for (Regatta regatta : service.getAllRegattas()) {
            service.stopTracking(regatta);
        }
    }

    public void serviceChanged(ServiceEvent ev) {
        ServiceReference<?> sr = ev.getServiceReference();
        System.out.println("service changed: "+ev+" for service reference "+sr);
    }
}
