package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateRaceDelayToLive extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -4759501337499106614L;
    private final long delayToLiveInMillis;
    
    public UpdateRaceDelayToLive(RegattaAndRaceIdentifier raceIdentifier, long delayToLiveInMillis) {
        super(raceIdentifier);
        this.delayToLiveInMillis = delayToLiveInMillis;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.setAndFixDelayToLiveInMillis(delayToLiveInMillis);
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
