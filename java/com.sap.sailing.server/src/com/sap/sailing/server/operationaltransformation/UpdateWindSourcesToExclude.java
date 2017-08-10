package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateWindSourcesToExclude extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 5599076261746041948L;
    private final Iterable<? extends WindSource> windSourcesToExclude;
    
    public UpdateWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<? extends WindSource> windSourcesToExclude) {
        super(raceIdentifier);
        this.windSourcesToExclude = windSourcesToExclude;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.getTrackedRace(getRaceIdentifier()).setWindSourcesToExclude(windSourcesToExclude);
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
