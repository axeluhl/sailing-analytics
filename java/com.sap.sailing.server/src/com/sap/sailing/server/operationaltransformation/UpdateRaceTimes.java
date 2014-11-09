package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateRaceTimes extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7054690983295760375L;
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
        trackedRace.setStartOfTrackingReceived(startOfTracking);
        trackedRace.setEndOfTrackingReceived(endOfTracking);
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
