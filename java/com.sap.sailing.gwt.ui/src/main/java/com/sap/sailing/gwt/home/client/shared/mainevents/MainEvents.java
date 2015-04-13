package com.sap.sailing.gwt.home.client.shared.mainevents;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.events.recent.RecentEventTeaser;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;

public class MainEvents extends Composite {
    interface MainEventsUiBinder extends UiBinder<Widget, MainEvents> {
    }
    
    private static MainEventsUiBinder uiBinder = GWT.create(MainEventsUiBinder.class);

    @UiField FlowPanel recentEventsTeaserPanel;
    @UiField Anchor showAllEventsAnchor;
    
    private final HomePlacesNavigator navigator;
    private final PlaceNavigation<EventsPlace> eventsNavigation;
    
    public MainEvents(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        
        MainEventsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        eventsNavigation = navigator.getEventsNavigation();
        showAllEventsAnchor.setHref(eventsNavigation.getTargetUrl());
        
    }

    public void setRecentEvents(List<EventListEventDTO> recentEvents) {
        final int MAX_RECENT_EVENTS_ON_HOME_PAGE = 3;
        recentEventsTeaserPanel.clear();
        for (int i=0; i<recentEvents.size() && i<MAX_RECENT_EVENTS_ON_HOME_PAGE; i++) {
            EventListEventDTO event = recentEvents.get(i);
            PlaceNavigation<EventDefaultPlace> eventNavigation = navigator.getEventNavigation(event.getId().toString(), event.getBaseURL(), event.isOnRemoteServer());
            RecentEventTeaser recentEvent = new RecentEventTeaser(eventNavigation, event, event.getState().getListStateMarker());
            recentEventsTeaserPanel.add(recentEvent);
        }
    }
    
    @UiHandler("showAllEventsAnchor")
    public void showAllEvents(ClickEvent e) {
        eventsNavigation.gotoPlace();
        e.preventDefault();
   }

}
