package com.sap.sailing.server;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator, ServiceListener {
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
        RacingEventService service = new RacingEventServiceImpl();

        Hashtable<String, ?> props = new Hashtable<String, String>();
        // register the service
        context.registerService(RacingEventService.class.getName(), service, props);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        fContext = null;
    }

    public void serviceChanged(ServiceEvent ev) {
        ServiceReference<?> sr = ev.getServiceReference();
        System.out.println("service changed: "+ev+" for service reference "+sr);
    }

}
