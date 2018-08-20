package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

public class EventReferenceWithStateDTO extends EventReferenceDTO {
    
    private EventState state;
    
    protected EventReferenceWithStateDTO() {
    }
    
    public EventReferenceWithStateDTO(UUID id, String name, EventState state) {
        super(id, name);
        this.state = state;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

}
