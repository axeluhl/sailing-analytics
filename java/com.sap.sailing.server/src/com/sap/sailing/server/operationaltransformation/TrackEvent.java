package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.server.RacingEventService;

public class TrackEvent extends AbstractRacingEventServiceOperation {
    private static final long serialVersionUID = 7891960455598410633L;
    private final EventIdentifier eventIdentifier;
    
    public TrackEvent(EventIdentifier eventIdentifier) {
        super();
        this.eventIdentifier = eventIdentifier;
    }
    
    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        Event event = toState.getEvent(eventIdentifier);
        toState.getOrCreateTrackedEvent(event);
        return toState;
    }

}
