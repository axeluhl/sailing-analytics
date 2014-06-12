package com.sap.sailing.gwt.home.client.place.events.recent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.DateUtil;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.recentevent.RecentEvents;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewRecent extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecent> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final PlaceNavigator navigator;

    @UiField HTMLPanel htmlPanel;
    
    private final Map<Integer, RecentEvents> recentEventsComposites;
    
    public EventsOverviewRecent(PlaceNavigator navigator) {
        this.navigator = navigator;
        recentEventsComposites = new HashMap<Integer, RecentEvents>();
        
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void updateEvents(Map<Integer, List<EventBaseDTO>> recentEventsOrderedByYear) {
        // remove old widgets
        htmlPanel.clear();
        recentEventsComposites.clear();

        // recent events of this year
        Date now = new Date();
        int currentYear = DateUtil.getYear(now);
        
        if(recentEventsOrderedByYear.get(currentYear) != null) {
            RecentEvents recentEvents = new RecentEvents(navigator);
            recentEvents.setEvents(recentEventsOrderedByYear.get(currentYear), null);
            htmlPanel.add(recentEvents);
            recentEventsComposites.put(currentYear, recentEvents);
        }
        currentYear--;
        while (currentYear > 2010) {
            if(recentEventsOrderedByYear.get(currentYear) != null) {
                RecentEvents recentEvents = new RecentEvents(navigator);
                recentEvents.setEvents(recentEventsOrderedByYear.get(currentYear), String.valueOf(currentYear));
                htmlPanel.add(recentEvents);
                recentEventsComposites.put(currentYear, recentEvents);
            }            
            currentYear--;
        }
    }
}
