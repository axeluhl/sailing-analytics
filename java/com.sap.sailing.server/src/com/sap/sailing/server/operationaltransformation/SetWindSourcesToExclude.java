package com.sap.sailing.server.operationaltransformation;

import java.util.Set;

import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;

public class SetWindSourcesToExclude extends AbstractRaceOperation {
    private static final long serialVersionUID = 7639288885720509529L;
    private final Set<WindSource> windSourcesToExclude;
    
    public SetWindSourcesToExclude(EventAndRaceIdentifier raceIdentifier, Set<WindSource> windSourcesToExclude) {
        super(raceIdentifier);
        this.windSourcesToExclude = windSourcesToExclude;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
        if (trackedRace != null) {
            trackedRace.setWindSourcesToExclude(windSourcesToExclude);
        }
        return toState;
    }

}
