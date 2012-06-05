package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddDefaultRegatta extends AbstractAddRegattaOperation {
    private static final long serialVersionUID = -3550383541066673065L;
    
    public AddDefaultRegatta(String regattaName, String boatClassName, boolean boatClassTypicallyStartsUpwind) {
        super(regattaName, boatClassName, boatClassTypicallyStartsUpwind);
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
        return toState.getOrCreateRegatta(getBaseEventName(), getBoatClassName(), isBoatClassTypicallyStartsUpwind());
    }

}
