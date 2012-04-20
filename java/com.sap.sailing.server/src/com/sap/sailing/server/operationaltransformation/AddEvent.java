package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddEvent extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -3550383541066673065L;
    private final String eventName;
    private final String boatClassName;
    private final boolean boatClassTypicallyStartsUpwind;
    
    public AddEvent(String eventName, String boatClassName, boolean boatClassTypicallyStartsUpwind) {
        super();
        this.eventName = eventName;
        this.boatClassName = boatClassName;
        this.boatClassTypicallyStartsUpwind = boatClassTypicallyStartsUpwind;
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

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.createEvent(eventName, boatClassName, boatClassTypicallyStartsUpwind);
        return null;
    }

}
