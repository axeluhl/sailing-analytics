package com.sap.sailing.gwt.home.communication.eventview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.gwt.home.communication.event.EventAndLeaderboardReferenceWithStateDTO;

public class SeriesReferenceWithEventsDTO extends SeriesReferenceDTO {

    private ArrayList<EventAndLeaderboardReferenceWithStateDTO> eventsOfSeries = new ArrayList<>();

    protected SeriesReferenceWithEventsDTO() {
    }

    public SeriesReferenceWithEventsDTO(String seriesDisplayName, String seriesLeaderboardGroupName,
            Collection<EventAndLeaderboardReferenceWithStateDTO> eventsOfSeries) {
        super(seriesDisplayName, seriesLeaderboardGroupName);
        this.eventsOfSeries.addAll(eventsOfSeries);
    }

    public List<EventAndLeaderboardReferenceWithStateDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }
}
