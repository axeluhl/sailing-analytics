package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;

public abstract class AbstractAddRegattaOperation extends AbstractRacingEventServiceOperation<Regatta> {
    private static final long serialVersionUID = 4596134166642095364L;
    private final String baseEventName;
    private final String boatClassName;
    private final boolean boatClassTypicallyStartsUpwind;
    
    public AbstractAddRegattaOperation(String regattaName, String boatClassName, boolean boatClassTypicallyStartsUpwind) {
        super();
        this.baseEventName = regattaName;
        this.boatClassName = boatClassName;
        this.boatClassTypicallyStartsUpwind = boatClassTypicallyStartsUpwind;
    }

    protected String getBaseEventName() {
        return baseEventName;
    }

    protected String getBoatClassName() {
        return boatClassName;
    }

    protected boolean isBoatClassTypicallyStartsUpwind() {
        return boatClassTypicallyStartsUpwind;
    }

}
