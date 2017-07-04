package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

public abstract class AbstractEventOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 1200611694004927369L;
    private final UUID id;

    public AbstractEventOperation(UUID id) {
        super();
        this.id = id;
    }

    protected boolean affectsSameEvent(AbstractEventOperation<?> other) {
        return getId().equals(other.getId());
    }
    
    protected UUID getId() {
        return id;
    }
    
}
