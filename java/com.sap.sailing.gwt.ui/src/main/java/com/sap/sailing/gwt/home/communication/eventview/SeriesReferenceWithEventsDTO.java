package com.sap.sailing.gwt.home.communication.eventview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.gwt.home.communication.event.EventAndLeaderboardReferenceWithStateDTO;

public class SeriesReferenceWithEventsDTO extends SeriesReferenceDTO {

    private ArrayList<EventAndLeaderboardReferenceWithStateDTO> eventsOfSeries = new ArrayList<>();

    protected SeriesReferenceWithEventsDTO() {
    }

    public SeriesReferenceWithEventsDTO(String seriesDisplayName, UUID seriesLeaderboardGroupId,
            Collection<EventAndLeaderboardReferenceWithStateDTO> eventsOfSeries) {
        super(seriesDisplayName, seriesLeaderboardGroupId);
        this.eventsOfSeries.addAll(eventsOfSeries);
    }

    public List<EventAndLeaderboardReferenceWithStateDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }
}
