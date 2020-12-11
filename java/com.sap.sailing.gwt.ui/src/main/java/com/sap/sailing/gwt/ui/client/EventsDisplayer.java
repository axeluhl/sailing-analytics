package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventsDisplayer {
    void fillEvents(List<EventDTO> events);
}
