package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;

public interface EventsView extends IsWidget {
    void setEvents(EventListViewDTO eventListView);
}
