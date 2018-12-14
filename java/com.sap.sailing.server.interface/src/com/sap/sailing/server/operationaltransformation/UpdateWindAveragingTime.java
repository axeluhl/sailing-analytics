package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class UpdateWindAveragingTime extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -3143003305202654846L;
    private static final Logger logger = Logger.getLogger(UpdateWindAveragingTime.class.getName());
    private final long newMillisecondsOverWhichToAverageWind;
    
    public UpdateWindAveragingTime(RegattaAndRaceIdentifier raceIdentifier, long newMillisecondsOverWhichToAverageWind) {
        super(raceIdentifier);
        this.newMillisecondsOverWhichToAverageWind = newMillisecondsOverWhichToAverageWind;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
        if (trackedRace != null) {
            trackedRace.setMillisecondsOverWhichToAverageWind(newMillisecondsOverWhichToAverageWind);
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
