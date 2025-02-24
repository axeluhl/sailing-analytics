package com.sap.sailing.aiagent;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.ChatSession;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * When activated, starts to track the {@link RacingEventService} using an OSGi {@link ServiceTracker} and maintains
 * an {@link AICore} instance to obtain {@link ChatSession}s from. It creates and registers an {@link AIAgent} with
 * the service registry. The agent can then be told to monitor one or more {@link Event}(s) or {@link Regatta}s and
 * will then attach itself as a {@link RaceChangeListener} to the races contained.
 * 
 * @author Axel Uhl (d043530)
 *
 */
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
