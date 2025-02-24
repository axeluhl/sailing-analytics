package com.sap.sailing.aiagent;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.aicore.AICore;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private static Activator instance;
    
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private AICore aiCore;

    public Activator() {
        instance = this;
    }
    
    static BundleContext getContext() {
        return context;
    }
    
    public static Activator getInstance() {
        return instance;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        aiCore = AICore.getDefault();
    }
    
    public RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    public AICore getAICore() {
        return aiCore;
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
