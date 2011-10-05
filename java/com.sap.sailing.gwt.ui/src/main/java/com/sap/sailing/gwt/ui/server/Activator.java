package com.sap.sailing.gwt.ui.server;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.httpservicetracker.HttpServiceTracker;
import com.sap.sailing.server.AdminApp;
import com.sap.sailing.server.ModeratorApp;
import com.sap.sailing.server.XcelsiusApp;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private HttpServiceTracker httpServiceTracker;
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        // now track the HTTP service:
        Map<String, Class<? extends javax.servlet.Servlet>> pathMap = new HashMap<String, Class<? extends javax.servlet.Servlet>>();
        pathMap.put("/admin", AdminApp.class);
        pathMap.put("/moderator", ModeratorApp.class);
        pathMap.put("/xcelsius", XcelsiusApp.class);
        httpServiceTracker = new HttpServiceTracker(context, pathMap, null);
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
