package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.RacingEventService;

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

    @Override
    public String toString() {
        return super.toString()+" [raceIdentifier=" + raceIdentifier + "]";
    }
}
