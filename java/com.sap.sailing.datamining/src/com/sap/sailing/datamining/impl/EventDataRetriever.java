package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;

public class EventDataRetriever implements DataRetriever<Event> {

    private Event event;
    private RacingEventService racingEventService;

    public EventDataRetriever(Event event, RacingEventService racingEventService) {
        this.event = event;
    }

    @Override
    public Event getTarget() {
        return event;
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Regatta regatta : getTarget().getRegattas()) {
            TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                data.addAll(new TrackedRegattaDataRetriever(trackedRegatta).retrieveData());
            }
        }
        return data;
    }

}
