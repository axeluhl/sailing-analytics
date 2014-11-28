package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;

public abstract class AbstractAddRegattaOperation extends AbstractRacingEventServiceOperation<Regatta> {
    private static final long serialVersionUID = 4596134166642095364L;
    private final String regattaName;
    private final String boatClassName;
    private final Serializable id;
    
    public AbstractAddRegattaOperation(String regattaName, String boatClassName, Serializable id) {
        super();
        this.regattaName = regattaName;
        this.boatClassName = boatClassName;
        this.id = id;
    }

    /**
     * Implementations are expected to replicate their effects during
     * {@link #internalApplyTo(com.sap.sailing.server.RacingEventService)}.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }

    protected String getRegattaName() {
        return regattaName;
    }

    protected String getBoatClassName() {
        return boatClassName;
    }
    
    protected Serializable getId() {
        return id;
    }
}
