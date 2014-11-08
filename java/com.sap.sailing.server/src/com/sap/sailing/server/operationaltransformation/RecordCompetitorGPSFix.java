package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordCompetitorGPSFix extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 5847067037829132465L;
    private final Serializable competitorID;
    private final GPSFixMoving gpsFix;
    
    public RecordCompetitorGPSFix(RegattaAndRaceIdentifier raceIdentifier, Competitor competitor, GPSFixMoving gpsFix) {
        super(raceIdentifier);
        this.competitorID = competitor.getId();
        this.gpsFix = gpsFix;
    }

    /**
     * Operations of this type can be run in parallel to other operations; subsequent operations do not have to wait
     * for this operation's completion.
     */
    @Override
    public boolean requiresSynchronousExecution() {
        return false;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        Competitor competitor = trackedRace.getRace().getCompetitorById(competitorID);
        trackedRace.recordFix(competitor, gpsFix);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<Void> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<Void> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
