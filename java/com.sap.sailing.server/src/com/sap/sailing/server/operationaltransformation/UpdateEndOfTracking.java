package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateEndOfTracking extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7054690983295760375L;
    private static final Logger logger = Logger.getLogger(UpdateEndOfTracking.class.getName());
    private final TimePoint endOfTracking;
    
    public UpdateEndOfTracking(RegattaAndRaceIdentifier raceIdentifier, TimePoint endOfTracking) {
        super(raceIdentifier);
        this.endOfTracking = endOfTracking;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        logger.fine("applying endOfTracking="+endOfTracking+" for "+trackedRace.getRace().getName());
        trackedRace.setEndOfTrackingReceived(endOfTracking);
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
