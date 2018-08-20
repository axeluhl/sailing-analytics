package com.sap.sailing.server.anniversary;

import com.sap.sailing.server.RacingEventService;
import com.sap.sse.replication.OperationWithResult;

/**
 * Operation for replication of the race count known by {@link AnniversaryRaceDeterminator}.
 */
public class UpdateRaceCountOperation implements OperationWithResult<RacingEventService, Void> {
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

}
