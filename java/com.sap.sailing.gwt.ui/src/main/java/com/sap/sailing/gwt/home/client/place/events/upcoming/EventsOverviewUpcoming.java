package com.sap.sailing.gwt.home.client.place.events.upcoming;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewUpcoming extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewUpcoming> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final PlaceNavigator navigator;

    public EventsOverviewUpcoming(PlaceNavigator navigator) {
        this.navigator = navigator;

        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void updateEvents(List<EventBaseDTO> events) {
        
    }

}
