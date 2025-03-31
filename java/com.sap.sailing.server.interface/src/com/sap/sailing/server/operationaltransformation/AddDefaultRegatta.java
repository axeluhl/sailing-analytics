package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class AddDefaultRegatta extends AbstractAddRegattaOperation {
    private static final long serialVersionUID = -3550383541066673065L;
    
    public AddDefaultRegatta(String regattaName, String boatClassName, TimePoint startDate, TimePoint endDate, Serializable id) {
        super(regattaName, boatClassName, startDate, endDate, id);
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
        return toState.getOrCreateDefaultRegatta(getRegattaName(), getBoatClassName(), getId());
    }

}
