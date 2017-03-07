package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface StartViewSixtyInch {
    Widget asWidget();
    
    void setEvents(List<EventDTO> events);
}
