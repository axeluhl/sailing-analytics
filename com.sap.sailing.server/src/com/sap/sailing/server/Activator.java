package com.sap.sailing.server;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator, ServiceListener {

    private RacingEventService service;
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private static BundleContext fContext;
    
    static BundleContext getDefault() {
        return fContext;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        fContext = context;
        service = new RacingEventServiceImpl();

        Hashtable<String, ?> props = new Hashtable<String, String>();
        // register the service
        context.registerService(RacingEventService.class.getName(), service, props);

        // create a tracker and track the service
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();

        // have a service listener to implement the whiteboard pattern
        fContext.addServiceListener(this, "(objectclass=" + RacingEventService.class.getName() + ")");

        // grab the service
        service = (RacingEventService) racingEventServiceTracker.getService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        // close the service tracker
        racingEventServiceTracker.close();
        racingEventServiceTracker = null;
        service = null;
        fContext = null;
    }

    public void serviceChanged(ServiceEvent ev) {
        ServiceReference<?> sr = ev.getServiceReference();
        System.out.println("service changed: "+ev+" for service reference "+sr);
    }

}
