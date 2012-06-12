package com.sap.sailing.server.operationaltransformation;

public abstract class AbstractEventOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = 1200611694004927369L;
    private final String eventName;
    
    public AbstractEventOperation(String eventName) {
        super();
        this.eventName = eventName;
    }

    protected String getEventName() {
        return eventName;
    }

    protected boolean affectsSameEvent(AbstractEventOperation<?> other) {
        return getEventName().equals(other.getEventName());
    }

}
