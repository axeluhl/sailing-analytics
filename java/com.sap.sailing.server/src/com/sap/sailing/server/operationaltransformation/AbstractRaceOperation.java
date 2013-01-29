package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public abstract class AbstractRaceOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -1162468486451355784L;
    private RegattaAndRaceIdentifier raceIdentifier;

    public AbstractRaceOperation(RegattaAndRaceIdentifier raceIdentifier) {
        super();
        this.raceIdentifier = raceIdentifier;
    }
    
    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

}
