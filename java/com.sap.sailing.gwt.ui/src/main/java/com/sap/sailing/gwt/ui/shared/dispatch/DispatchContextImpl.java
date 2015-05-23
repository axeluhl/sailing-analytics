package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.server.RacingEventService;

@GwtIncompatible
public class DispatchContextImpl implements DispatchContext {
    private final RacingEventService racingEventService;
    private final Date currentClientTime;
    private final Date currentServerTime = new Date();

    public  DispatchContextImpl(Date currentClientTime, RacingEventService racingEventService) {
        this.currentClientTime = currentClientTime;
        this.racingEventService = racingEventService;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventService;
    }
    
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
    
    public Date getCurrentServerTime() {
        return currentServerTime;
    }
}
