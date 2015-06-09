package com.sap.sailing.gwt.home.mobile.places.events;

import java.util.UUID;

import com.google.gwt.user.client.ui.Widget;

public interface EventsView {

    Widget asWidget();
    
    public interface Presenter {
        void gotoTheEvent(UUID eventId);
    }
}

