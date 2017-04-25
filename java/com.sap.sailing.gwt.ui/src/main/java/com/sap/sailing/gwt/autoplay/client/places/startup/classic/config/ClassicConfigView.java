package com.sap.sailing.gwt.autoplay.client.places.startup.classic.config;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface ClassicConfigView {
    public interface Presenter {
    }

    Widget asWidget();
    
    void setEvents(List<EventDTO> events);
}
