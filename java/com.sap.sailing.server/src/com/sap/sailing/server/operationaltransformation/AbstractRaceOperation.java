package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.EventAndRaceIdentifier;

public abstract class AbstractRaceOperation extends AbstractRacingEventServiceOperation {
    private static final long serialVersionUID = -1162468486451355784L;
    private EventAndRaceIdentifier raceIdentifier;

    public AbstractRaceOperation(EventAndRaceIdentifier raceIdentifier) {
        super();
        this.raceIdentifier = raceIdentifier;
    }
    
    protected EventAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

}
