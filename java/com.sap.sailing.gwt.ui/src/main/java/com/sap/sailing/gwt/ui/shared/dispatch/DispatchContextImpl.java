package com.sap.sailing.gwt.ui.shared.dispatch;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.util.ServiceTrackerFactory;


public class DispatchContextImpl implements DispatchContext {

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public  DispatchContextImpl() {
        final BundleContext context = Activator.getDefault();
//        final Activator activator = Activator.getInstance();

        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
}
