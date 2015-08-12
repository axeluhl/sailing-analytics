package com.sap.sailing.gwt.ui.shared.eventlist;

import com.sap.sailing.gwt.ui.shared.general.EventLinkAndMetadataDTO;

public class EventListEventDTO extends EventLinkAndMetadataDTO {
    
    private EventListEventSeriesDTO eventSeries;

    public EventListEventSeriesDTO getEventSeries() {
        return eventSeries;
    }

    public void setEventSeries(EventListEventSeriesDTO eventSeries) {
        this.eventSeries = eventSeries;
    }
    
}
