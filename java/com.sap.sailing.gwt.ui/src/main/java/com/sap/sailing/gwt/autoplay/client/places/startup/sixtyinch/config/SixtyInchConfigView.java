package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface SixtyInchConfigView {
    public interface Presenter {
    }

    Widget asWidget();
    
    void setEvents(List<EventDTO> events);
}
