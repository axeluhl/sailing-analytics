package com.sap.sailing.gwt.home.communication.eventlist;

import com.sap.sailing.gwt.home.communication.event.EventLinkAndMetadataDTO;

public class EventListEventDTO extends EventLinkAndMetadataDTO {
    
    private EventListEventSeriesDTO eventSeries;

    @Override
    public EventListEventSeriesDTO getEventSeries() {
        return eventSeries;
    }

    public void setEventSeries(EventListEventSeriesDTO eventSeries) {
        this.eventSeries = eventSeries;
    }
    
}
