package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateCompetitorToBoatAssignment extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 3852565541729661410L;
    private final Competitor competitor;
    private final Boat boat;
    
    public UpdateCompetitorToBoatAssignment(RegattaAndRaceIdentifier raceIdentifier, Competitor competitor, Boat boat) {
        super(raceIdentifier);
        this.competitor = competitor;
        this.boat = boat;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.setBoatForCompetitor(competitor, boat);
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
