package com.sap.sailing.gwt.home.client.app.events;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventsView {
    Widget asWidget();
    
    void setEvents(Iterable<EventDTO> events);
}
