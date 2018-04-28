package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateStartOfTracking extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7054690983295760375L;
    private static final Logger logger = Logger.getLogger(UpdateStartOfTracking.class.getName());
    private final TimePoint startOfTracking;
    
    public UpdateStartOfTracking(RegattaAndRaceIdentifier raceIdentifier, TimePoint startOfTracking) {
        super(raceIdentifier);
        this.startOfTracking = startOfTracking;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
        if (trackedRace != null) {
            logger.fine("applying startOfTracking="+startOfTracking+" for "+trackedRace.getRace().getName());
            trackedRace.setStartOfTrackingReceived(startOfTracking);
        } else {
            logger.warning("Tracked race for "+getRaceIdentifier()+" has disappeared");
        }
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
