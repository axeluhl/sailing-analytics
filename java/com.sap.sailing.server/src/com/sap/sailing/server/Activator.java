package com.sap.sailing.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.httpservicetracker.HttpServiceTracker;
import com.sap.sailing.util.Util.Triple;

public class Activator implements BundleActivator, ServiceListener {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static BundleContext fContext;
    
    private HttpServiceTracker httpServiceTracker;
    
    static BundleContext getDefault() {
        return fContext;
    }

    public void start(BundleContext context) throws Exception {
        fContext = context;
        RacingEventService service = new RacingEventServiceImpl();
        Hashtable<String, ?> props = new Hashtable<String, String>();
        // register the racing service
        context.registerService(RacingEventService.class.getName(), service, props);
        
        // now track the HTTP service:
        Map<String, Class<? extends javax.servlet.Servlet>> pathMap = new HashMap<String, Class<? extends javax.servlet.Servlet>>();
        pathMap.put("/admin", AdminApp.class);
        pathMap.put("/moderator", ModeratorApp.class);
        httpServiceTracker = new HttpServiceTracker(context, pathMap, null);
        httpServiceTracker.open();
        logger.log(Level.INFO, "Started "+context.getBundle().getSymbolicName()+". Character encoding: "+
                Charset.defaultCharset());
    }
    
    public void stop(BundleContext context) throws Exception {
        fContext = null;
        ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        // grab the service
        RacingEventService service = (RacingEventService) racingEventServiceTracker.getService();
        for (Triple<Event, RaceDefinition, String> windTracker : service.getWindTrackedRaces()) {
            service.stopTrackingWind(windTracker.getA(), windTracker.getB());
        }
        for (Event event : service.getAllEvents()) {
            service.stopTracking(event);
        }
        // stop tracking the HTTP service:
        httpServiceTracker.close();
        httpServiceTracker = null;
    }

    public void serviceChanged(ServiceEvent ev) {
        ServiceReference<?> sr = ev.getServiceReference();
        System.out.println("service changed: "+ev+" for service reference "+sr);
    }
}
