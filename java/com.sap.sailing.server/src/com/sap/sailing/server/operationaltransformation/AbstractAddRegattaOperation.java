package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;

public abstract class AbstractAddRegattaOperation extends AbstractRacingEventServiceOperation<Regatta> {
    private static final long serialVersionUID = 4596134166642095364L;
    private final String baseEventName;
    private final String boatClassName;
    
    public AbstractAddRegattaOperation(String regattaName, String boatClassName) {
        super();
        this.baseEventName = regattaName;
        this.boatClassName = boatClassName;
    }

    protected String getBaseEventName() {
        return baseEventName;
    }

    protected String getBoatClassName() {
        return boatClassName;
    }
}
