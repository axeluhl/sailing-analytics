package com.sap.sailing.gwt.home.client.place.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.client.DateUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public abstract class AbstractEventsView extends Composite implements EventsView {
    private final Map<Integer, List<EventBaseDTO>> recentEventsOrderedByYear;
    private final List<EventBaseDTO> upcomingEvents;

    public AbstractEventsView() {
        recentEventsOrderedByYear = new HashMap<Integer, List<EventBaseDTO>>();
        upcomingEvents = new ArrayList<EventBaseDTO>();
    }
    
    @Override
    public void setEvents(List<EventBaseDTO> events) {
        for(EventBaseDTO event: events) {
            if(event.startDate != null) {
                if(DateUtil.isDayInPast(event.startDate)) { 
                    // recent event
                    int year = DateUtil.getYear(event.startDate);
                    List<EventBaseDTO> eventsOfYear = recentEventsOrderedByYear.get(year);
                    if(eventsOfYear == null) {
                        eventsOfYear = new ArrayList<EventBaseDTO>();
                        recentEventsOrderedByYear.put(year, eventsOfYear);
                    }
                    eventsOfYear.add(event);
                } else {
                    // upcoming event
                    upcomingEvents.add(event);
                }
            }
        }
        updateEventsUI();
    }

    protected abstract void updateEventsUI();

    public Map<Integer, List<EventBaseDTO>> getRecentEventsOrderedByYear() {
        return recentEventsOrderedByYear;
    }

    public List<EventBaseDTO> getUpcomingEvents() {
        return upcomingEvents;
    }
}
