package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateWindAveragingTime extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -3143003305202654846L;
    private final long newMillisecondsOverWhichToAverageWind;
    
    public UpdateWindAveragingTime(RegattaAndRaceIdentifier raceIdentifier, long newMillisecondsOverWhichToAverageWind) {
        super(raceIdentifier);
        this.newMillisecondsOverWhichToAverageWind = newMillisecondsOverWhichToAverageWind;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.setMillisecondsOverWhichToAverageWind(newMillisecondsOverWhichToAverageWind);
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
