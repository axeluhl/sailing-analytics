package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddRegatta extends AbstractRacingEventServiceOperation<Regatta> {
    private static final long serialVersionUID = -3550383541066673065L;
    private final String baseEventName;
    private final String boatClassName;
    private final boolean boatClassTypicallyStartsUpwind;
    
    public AddRegatta(String regattaName, String boatClassName, boolean boatClassTypicallyStartsUpwind) {
        super();
        this.baseEventName = regattaName;
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
    public Regatta internalApplyTo(RacingEventService toState) {
        return toState.getOrCreateRegatta(baseEventName, boatClassName, boatClassTypicallyStartsUpwind);
    }

}
