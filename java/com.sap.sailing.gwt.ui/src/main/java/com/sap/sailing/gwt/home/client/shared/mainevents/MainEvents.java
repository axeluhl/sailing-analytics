package com.sap.sailing.gwt.home.client.shared.mainevents;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class MainEvents extends Composite {

    private List<EventDTO> recentEvents;
    
    @UiField Label eventName;
    @UiField Label venueName;
    @UiField Label eventStartDate;
    @UiField Anchor linkToEventOverview;
    @UiField Image eventImage;

    interface MainEventsUiBinder extends UiBinder<Widget, MainEvents> {
    }
    
    private static MainEventsUiBinder uiBinder = GWT.create(MainEventsUiBinder.class);

    public MainEvents() {
        MainEventsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        recentEvents = new ArrayList<EventDTO>();
    }

    public void setRecentEvents(List<EventDTO> theRecentEvents) {
        recentEvents.clear();
        recentEvents.addAll(theRecentEvents);
        
        if(recentEvents.size() > 0) {
            EventDTO firstRecentEvent = recentEvents.get(0);
            eventName.setText(firstRecentEvent.getName());
            venueName.setText(firstRecentEvent.venue.getName());
            eventStartDate.setText(firstRecentEvent.startDate.toString());
            eventImage.setUrl("http://www.sapsailing.com/icon.png");
            
        }
    }
}
