package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;

public class SetRaceIsKnownToStartUpwind extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 272403191741207144L;
    private final boolean startsUpwind;
    
    public SetRaceIsKnownToStartUpwind(EventAndRaceIdentifier raceIdentifier, boolean startsUpwind) {
        super(raceIdentifier);
        this.startsUpwind = startsUpwind;
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
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
        if (trackedRace != null) {
            trackedRace.setRaceIsKnownToStartUpwind(startsUpwind);
        }
        return null;
    }

}
