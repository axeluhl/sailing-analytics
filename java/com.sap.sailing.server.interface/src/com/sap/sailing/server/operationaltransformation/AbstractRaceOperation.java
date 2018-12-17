package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractRaceOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -1162468486451355784L;
    private RegattaAndRaceIdentifier raceIdentifier;
    
    public AbstractRaceOperation(RegattaAndRaceIdentifier raceIdentifier) {
        super();
        this.raceIdentifier = raceIdentifier;
    }
    
    /**
     * The default for race operations is that their {@link #internalApplyTo(RacingEventService)} method already
     * replicates the operation's effects.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }
    
    protected DynamicTrackedRace getTrackedRace(RacingEventService racingEventService) {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) racingEventService.getExistingTrackedRace(getRaceIdentifier());
        return trackedRace;
    }

    @Override
    public String toString() {
        return super.toString()+" [raceIdentifier=" + raceIdentifier + "]";
    }
}
