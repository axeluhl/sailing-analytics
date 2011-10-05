package com.sap.sailing.gwt.ui.server;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.httpservicetracker.HttpServiceTracker;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private HttpServiceTracker httpServiceTracker;
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        // now track the HTTP service:
        Map<String, Class<? extends javax.servlet.Servlet>> pathMap = new HashMap<String, Class<? extends javax.servlet.Servlet>>();
        pathMap.put("/sailing", SailingServiceImpl.class);
        Map<String, String> resourceMap = new HashMap<String, String>();
        resourceMap.put("/", "/war");
        httpServiceTracker = new HttpServiceTracker(context, pathMap, resourceMap);
        httpServiceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // stop tracking the HTTP service:
        httpServiceTracker.close();
        httpServiceTracker = null;
    }
    
    public static BundleContext getDefault() {
        return context;
    }

}
