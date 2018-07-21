package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class SetRaceIsKnownToStartUpwind extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 272403191741207144L;
    private final boolean startsUpwind;
    
    public SetRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean startsUpwind) {
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

    /**
     * {@link #internalApplyTo(RacingEventService)} does not replicate the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return true;
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
