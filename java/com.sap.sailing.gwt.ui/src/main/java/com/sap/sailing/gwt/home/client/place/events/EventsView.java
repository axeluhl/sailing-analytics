package com.sap.sailing.gwt.home.client.place.events;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public interface EventsView {
    Widget asWidget();
    
    void setEvents(List<EventBaseDTO> events);
}
