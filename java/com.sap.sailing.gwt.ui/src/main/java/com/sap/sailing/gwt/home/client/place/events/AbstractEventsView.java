package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;

public abstract class AbstractEventsView extends Composite implements EventsView {
    protected EventListViewDTO eventListView;
    
    @Override
    public void setEvents(EventListViewDTO eventListView) {
        this.eventListView = eventListView;
        updateEventsUI();
    }

    protected abstract void updateEventsUI();
}
