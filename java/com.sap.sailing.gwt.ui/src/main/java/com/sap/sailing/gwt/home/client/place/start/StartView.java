package com.sap.sailing.gwt.home.client.place.start;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface StartView {
    Widget asWidget();
    
    void setFeaturedEvent(EventDTO featuredEvent);

    void setRecentEvents(List<EventDTO> recentEvents);
}
