package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateRaceTimes extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7054690983295760375L;
    private static final Logger logger = Logger.getLogger(UpdateRaceTimes.class.getName());
    private final TimePoint startOfTracking;
    private final TimePoint endOfTracking;
    private final TimePoint startTimeReceived;
    
    public UpdateRaceTimes(RegattaAndRaceIdentifier raceIdentifier, TimePoint startOfTracking, TimePoint endOfTracking,
            TimePoint startTimeReceived) {
        super(raceIdentifier);
        this.startOfTracking = startOfTracking;
        this.endOfTracking = endOfTracking;
        this.startTimeReceived = startTimeReceived;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        logger.fine("applying startOfTracking="+startOfTracking+" for "+trackedRace.getRace().getName());
        trackedRace.setStartOfTrackingReceived(startOfTracking);
        logger.fine("applying endOfTracking="+endOfTracking+" for "+trackedRace.getRace().getName());
        trackedRace.setEndOfTrackingReceived(endOfTracking);
        logger.fine("applying startTimeReceived="+startTimeReceived+" for "+trackedRace.getRace().getName());
        trackedRace.setStartTimeReceived(startTimeReceived);
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
