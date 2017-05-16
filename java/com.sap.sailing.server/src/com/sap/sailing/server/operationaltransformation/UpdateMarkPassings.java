package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateMarkPassings extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 8462323149468755367L;
    private final Competitor competitor;
    private final Iterable<MarkPassing> markPassings;
    
    public UpdateMarkPassings(RegattaAndRaceIdentifier raceIdentifier, Competitor competitor,
            Iterable<MarkPassing> markPassings) {
        super(raceIdentifier);
        this.competitor = competitor;
        this.markPassings = markPassings;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.updateMarkPassings(competitor, markPassings);
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
