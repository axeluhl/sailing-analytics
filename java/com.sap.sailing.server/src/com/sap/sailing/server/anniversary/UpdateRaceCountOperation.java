package com.sap.sailing.server.anniversary;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.AbstractRacingEventServiceOperation;

/**
 * Operation for replication of the race count known by {@link AnniversaryRaceDeterminatorImpl}.
 */
public class UpdateRaceCountOperation extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 8989228640752085561L;
    private final int raceCount;
    
    public UpdateRaceCountOperation(int raceCount) {
        this.raceCount = raceCount;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.getAnniversaryRaceDeterminator().setRaceCount(raceCount);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}
