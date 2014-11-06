package com.sap.sailing.gwt.home.client.place.events.upcoming;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewUpcoming extends Composite {

    @UiField HTMLPanel eventsPlaceholder;
    
    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewUpcoming> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final HomePlacesNavigator navigator;

    private final List<UpcomingEvent> upcomingEventComposites;

    public EventsOverviewUpcoming(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        upcomingEventComposites = new ArrayList<UpcomingEvent>();
        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void updateEvents(List<EventBaseDTO> events) {
        eventsPlaceholder.clear();
        upcomingEventComposites.clear();
        for (EventBaseDTO event : events) {
            UpcomingEvent upcomingEvent = new UpcomingEvent(event, navigator);
            upcomingEventComposites.add(upcomingEvent);
            eventsPlaceholder.getElement().appendChild(upcomingEvent.getElement());
        }
    }

}
