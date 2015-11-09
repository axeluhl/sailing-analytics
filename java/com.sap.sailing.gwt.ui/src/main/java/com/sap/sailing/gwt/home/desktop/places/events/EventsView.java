package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.eventlist.EventListViewDTO;

public interface EventsView extends IsWidget {
    void setEvents(EventListViewDTO eventListView);
}
