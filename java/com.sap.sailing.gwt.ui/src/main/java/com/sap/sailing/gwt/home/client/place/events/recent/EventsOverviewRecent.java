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
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewRecent extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecent> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final PlaceNavigator navigator;

    @UiField HTMLPanel recentEventsPerYearPanel;
    
    private final Map<Integer, EventsOverviewRecentYear> recentEventsComposites;
    
    public EventsOverviewRecent(PlaceNavigator navigator) {
        this.navigator = navigator;
        recentEventsComposites = new HashMap<Integer, EventsOverviewRecentYear>();
        
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void updateEvents(Map<Integer, List<EventBaseDTO>> recentEventsOrderedByYear) {
        // remove old widgets
        recentEventsPerYearPanel.clear();
        recentEventsComposites.clear();

        // recent events of this year
        Date now = new Date();
        int currentYear = DateUtil.getYear(now);
        boolean oneYearIsExpanded = false;
        
        if(recentEventsOrderedByYear.get(currentYear) != null) {
            EventsOverviewRecentYear recentEventsOfOneYear = new EventsOverviewRecentYear(currentYear, recentEventsOrderedByYear.get(currentYear),  navigator);
            recentEventsPerYearPanel.add(recentEventsOfOneYear);
            recentEventsComposites.put(currentYear, recentEventsOfOneYear);
            recentEventsOfOneYear.showContent();
            oneYearIsExpanded = true;
        }
        currentYear--;
        while (currentYear > 2010) {
            if(recentEventsOrderedByYear.get(currentYear) != null) {
                EventsOverviewRecentYear recentEventsOfOneYear = new EventsOverviewRecentYear(currentYear, recentEventsOrderedByYear.get(currentYear), navigator);
                recentEventsPerYearPanel.add(recentEventsOfOneYear);
                recentEventsComposites.put(currentYear, recentEventsOfOneYear);
                if(oneYearIsExpanded == true) {
                    recentEventsOfOneYear.hideContent();
                } else {
                    recentEventsOfOneYear.showContent();
                    oneYearIsExpanded = true;
                }
            }            
            currentYear--;
        }
    }
}
