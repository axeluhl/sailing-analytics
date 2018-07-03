package com.sap.sailing.gwt.home.communication.eventview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.gwt.home.communication.event.EventReferenceWithStateDTO;

public class SeriesReferenceWithEventsDTO extends SeriesReferenceDTO {

    private ArrayList<EventReferenceWithStateDTO> eventsOfSeries = new ArrayList<>();

    protected SeriesReferenceWithEventsDTO() {
    }

    public SeriesReferenceWithEventsDTO(String seriesDisplayName, String seriesLeaderboardGroupName,
            Collection<EventReferenceWithStateDTO> eventsOfSeries) {
        super(seriesDisplayName, seriesLeaderboardGroupName);
        this.eventsOfSeries.addAll(eventsOfSeries);
    }

    public List<EventReferenceWithStateDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }
}
