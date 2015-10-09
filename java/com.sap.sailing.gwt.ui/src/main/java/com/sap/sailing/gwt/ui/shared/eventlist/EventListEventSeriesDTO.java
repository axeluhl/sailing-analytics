package com.sap.sailing.gwt.ui.shared.eventlist;

import java.util.UUID;

import com.sap.sailing.gwt.ui.shared.general.EventSeriesReferenceDTO;

public class EventListEventSeriesDTO extends EventSeriesReferenceDTO {
    
    private int eventsCount;
    
    protected EventListEventSeriesDTO() {
    }

    public EventListEventSeriesDTO(UUID id, String displayName) {
        super(id, displayName);
    }

    public int getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(int eventsCount) {
        this.eventsCount = eventsCount;
    }
    
}
