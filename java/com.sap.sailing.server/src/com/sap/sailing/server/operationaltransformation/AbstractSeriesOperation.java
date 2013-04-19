package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaIdentifier;

public abstract class AbstractSeriesOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -7786133902346491275L;
    private final RegattaIdentifier regattaIdentifier;
    private final String seriesName;
    
    public AbstractSeriesOperation(RegattaIdentifier regattaIdentifier, String seriesName) {
        super();
        this.regattaIdentifier = regattaIdentifier;
        this.seriesName = seriesName;
    }

    protected RegattaIdentifier getRegattaIdentifier() {
        return regattaIdentifier;
    }

    protected String getSeriesName() {
        return seriesName;
    }
    
}
