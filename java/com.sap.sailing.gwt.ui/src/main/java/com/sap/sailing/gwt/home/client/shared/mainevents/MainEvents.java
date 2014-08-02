package com.sap.sailing.gwt.home.client.shared.mainevents;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.recentevent.RecentEvent;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class MainEvents extends Composite {

    private List<EventBaseDTO> recentEvents;
    
    @UiField(provided=true) RecentEvent event1;
    @UiField(provided=true) RecentEvent event2;
    @UiField(provided=true) RecentEvent event3;

    interface MainEventsUiBinder extends UiBinder<Widget, MainEvents> {
    }
    
    private static MainEventsUiBinder uiBinder = GWT.create(MainEventsUiBinder.class);

    public MainEvents(PlaceNavigator navigator) {
        event1 = new RecentEvent(navigator);
        event2 = new RecentEvent(navigator);
        event3 = new RecentEvent(navigator);
        MainEventsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        recentEvents = new ArrayList<EventBaseDTO>();
    }

    public void setRecentEvents(List<EventBaseDTO> theRecentEvents) {
        recentEvents.clear();
        recentEvents.addAll(theRecentEvents);
        
        int size = recentEvents.size();
        if(size > 0) {
            event1.setEvent(recentEvents.get(0));
        } else {
            event1.setVisible(false);
        }
        if(size > 1) {
            event2.setEvent(recentEvents.get(1));
        } else {
            event2.setVisible(false);
        }
        if(size > 2) {
            event3.setEvent(recentEvents.get(2));
        } else {
            event3.setVisible(false);
        }
    }
}
