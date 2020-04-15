package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class SetWindSourcesToExclude extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7639288885720509529L;
    private final Iterable<WindSource> windSourcesToExclude;
    
    public SetWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude) {
        super(raceIdentifier);
        this.windSourcesToExclude = windSourcesToExclude;
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
            trackedRace.setWindSourcesToExclude(windSourcesToExclude);
        }
        return null;
    }

}
