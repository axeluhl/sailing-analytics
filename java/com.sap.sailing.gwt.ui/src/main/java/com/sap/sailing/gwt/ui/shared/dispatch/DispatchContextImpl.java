package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.RacingEventService;


public class DispatchContextImpl implements DispatchContext {

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final Date currentClientTime;
    private final Date currentServerTime = new Date();

    public  DispatchContextImpl(Date currentClientTime, ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker) {
        this.currentClientTime = currentClientTime;
        this.racingEventServiceTracker = racingEventServiceTracker;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
    
    public Date getCurrentServerTime() {
        return currentServerTime;
    }
}
