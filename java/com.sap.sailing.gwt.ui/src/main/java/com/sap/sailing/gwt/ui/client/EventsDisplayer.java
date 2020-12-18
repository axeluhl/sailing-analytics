package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventsDisplayer extends Displayer {
    void fillEvents(Iterable<EventDTO> events);
}
