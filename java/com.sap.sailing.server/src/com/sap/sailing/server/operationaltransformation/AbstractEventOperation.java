package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

public abstract class AbstractEventOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 1200611694004927369L;
    private final Serializable id;

    public AbstractEventOperation(Serializable id) {
        super();
        this.id = id;
    }

    protected boolean affectsSameEvent(AbstractEventOperation<?> other) {
        return getId().equals(other.getId());
    }
    
    protected Serializable getId() {
        return id;
    }
    
}
