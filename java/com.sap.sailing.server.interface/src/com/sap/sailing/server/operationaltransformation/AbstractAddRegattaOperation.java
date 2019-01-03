package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sse.common.TimePoint;

public abstract class AbstractAddRegattaOperation extends AbstractRacingEventServiceOperation<Regatta> {
    private static final long serialVersionUID = 4596134166642095364L;
    private final String regattaName;
    private final String boatClassName;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final Serializable id;
    
    public AbstractAddRegattaOperation(String regattaName, String boatClassName, TimePoint startDate, TimePoint endDate, Serializable id) {
        super();
        this.regattaName = regattaName;
        this.boatClassName = boatClassName;
        this.startDate = startDate;
        this.endDate = endDate;
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

    protected TimePoint getStartDate() {
        return startDate;
    }

    protected TimePoint getEndDate() {
        return endDate;
    }
}
