package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasEventContext;
import com.sap.sailing.domain.base.Event;

public class HasEventContextImpl implements HasEventContext {

    private final Event event;
    
    public HasEventContextImpl(Event event) {
        this.event = event;
    }

    @Override
    public Event getEvent() {
        return event;
    }

}
