package com.sap.sailing.gwt.home.communication.eventlist;

import java.util.UUID;

import com.sap.sailing.gwt.home.communication.event.EventSeriesReferenceDTO;

public class EventListEventSeriesDTO extends EventSeriesReferenceDTO {
    
    private int eventsCount;
    
    protected EventListEventSeriesDTO() {
    }

    public EventListEventSeriesDTO(String displayName, UUID seriesLeaderboardGroupId) {
        super(displayName, seriesLeaderboardGroupId);
    }

    public int getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(int eventsCount) {
        this.eventsCount = eventsCount;
    }
    
}
