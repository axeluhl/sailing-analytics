package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;

public class EventSelector implements Selector {
    
    private String[] eventNamesForSelection;

    public EventSelector(String... eventNames) {
        eventNamesForSelection = eventNames;
    }
    
    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Event event : racingEventService.getAllEvents()) {
            for (String eventName : eventNamesForSelection) {
                if (eventName.equals(event.getName())) {
                    data.addAll(new EventDataRetriever(event, racingEventService).retrieveData());
                }
            }
        }
        return data;
    }

}
