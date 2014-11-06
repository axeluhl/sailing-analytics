package com.sap.sailing.gwt.home.client.place.events.recent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewRecent extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecent> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final HomePlacesNavigator navigator;

    @UiField HTMLPanel recentEventsPerYearPanel;
    
    private final Map<Integer, EventsOverviewRecentYear> recentEventsComposites;
    
    public EventsOverviewRecent(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        recentEventsComposites = new HashMap<Integer, EventsOverviewRecentYear>();
        
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void updateEvents(Map<Integer, List<EventBaseDTO>> recentEventsByYearOrderedByEndDate) {
        // remove old widgets
        recentEventsPerYearPanel.clear();
        recentEventsComposites.clear();
        // recent events of this year
        LinkedHashMap<Integer, List<EventBaseDTO>> recentEventsOrderedDescendingByYearOrderedDescendingByEndDate =
                sortEventsDescendingByYear(recentEventsByYearOrderedByEndDate);
        boolean oneYearIsExpanded = false;
        for (Entry<Integer, List<EventBaseDTO>> e : recentEventsOrderedDescendingByYearOrderedDescendingByEndDate.entrySet()) {
            int currentYear = e.getKey();
            EventsOverviewRecentYear recentEventsOfOneYear = new EventsOverviewRecentYear(currentYear, e.getValue(), navigator);
            recentEventsPerYearPanel.add(recentEventsOfOneYear);
            recentEventsComposites.put(currentYear, recentEventsOfOneYear);
            if (oneYearIsExpanded == true) {
                recentEventsOfOneYear.hideContent();
            } else {
                recentEventsOfOneYear.showContent();
                oneYearIsExpanded = true;
            }
        }
    }

    private LinkedHashMap<Integer, List<EventBaseDTO>> sortEventsDescendingByYear(
            Map<Integer, List<EventBaseDTO>> recentEventsByYearOrderedByEndDate) {
        LinkedHashMap<Integer, List<EventBaseDTO>> result = new LinkedHashMap<>();
        List<Integer> yearKeysInDescendingOrder = new ArrayList<>(recentEventsByYearOrderedByEndDate.keySet());
        Collections.sort(yearKeysInDescendingOrder);
        for (ListIterator<Integer> i=yearKeysInDescendingOrder.listIterator(yearKeysInDescendingOrder.size()); i.hasPrevious(); ) {
            Integer year = i.previous();
            result.put(year, recentEventsByYearOrderedByEndDate.get(year));
        }
        return result;
    }
}
