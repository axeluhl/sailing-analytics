package com.sap.sailing.xcelsiusadapter;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.httpservicetracker.HttpServiceTracker;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    private HttpServiceTracker httpServiceTracker;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        // now track the HTTP service:
        Map<String, Class<? extends javax.servlet.Servlet>> pathMap = new HashMap<String, Class<? extends javax.servlet.Servlet>>();
        pathMap.put("/xcelsius", XcelsiusApp.class);
        httpServiceTracker = new HttpServiceTracker(context, pathMap, null);
        httpServiceTracker.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        // stop tracking the HTTP service:
        httpServiceTracker.close();
        httpServiceTracker = null;
    }

}
