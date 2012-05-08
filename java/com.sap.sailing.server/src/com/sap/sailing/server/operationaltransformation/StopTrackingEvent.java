package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class StopTrackingEvent extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -651066062098923320L;
    private final EventIdentifier eventIdentifier;

    public StopTrackingEvent(EventIdentifier eventIdentifier) {
        super();
        this.eventIdentifier = eventIdentifier;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws MalformedURLException, IOException, InterruptedException {
        Event event = toState.getEvent(eventIdentifier);
        if (event != null) {
            toState.stopTracking(event);
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
