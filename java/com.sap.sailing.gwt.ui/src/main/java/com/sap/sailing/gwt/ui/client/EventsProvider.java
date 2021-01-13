package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventsProvider {
    Iterable<EventDTO> getAllEvents();
}
