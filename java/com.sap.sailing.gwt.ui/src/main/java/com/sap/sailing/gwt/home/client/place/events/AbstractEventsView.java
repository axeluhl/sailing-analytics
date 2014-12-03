package com.sap.sailing.gwt.home.client.place.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public abstract class AbstractEventsView extends Composite implements EventsView {
    private final Map<Integer, List<EventBaseDTO>> recentEventsByYearOrderedByEndDate;
    private final List<EventBaseDTO> upcomingEvents;

    public AbstractEventsView() {
        recentEventsByYearOrderedByEndDate = new HashMap<Integer, List<EventBaseDTO>>();
        upcomingEvents = new ArrayList<EventBaseDTO>();
    }
    
    @Override
    public void setEvents(List<EventBaseDTO> events) {
        for (EventBaseDTO event : events) {
            if (event.startDate != null && event.endDate != null) {
                if (DateUtil.isDayInPast(event.startDate) || event.isRunning()) {
                    // recent event or live event
                    int year = DateUtil.getYear(event.startDate);
                    List<EventBaseDTO> eventsOfYear = recentEventsByYearOrderedByEndDate.get(year);
                    if (eventsOfYear == null) {
                        eventsOfYear = new ArrayList<EventBaseDTO>();
                        recentEventsByYearOrderedByEndDate.put(year, eventsOfYear);
                    }
                    eventsOfYear.add(event);
                } else {
                    // upcoming event
                    upcomingEvents.add(event);
                }
            }
        }
        for (List<EventBaseDTO> eventsPerYear : recentEventsByYearOrderedByEndDate.values()) {
            Collections.sort(eventsPerYear, EVENTS_BY_DESCENDING_DATE_COMPARATOR);
        }
        Collections.sort(upcomingEvents, EVENTS_BY_ASCENDING_DATE_COMPARATOR);
        updateEventsUI();
    }

    protected abstract void updateEventsUI();

    /**
     * Returns a map whose keys denote years and whose values are event lists ordered by descending
     * {@link EventBaseDTO#endDate end date}. Note that the iteration order for the map entries is undefined
     * and in particular is not enumerating the years in any well-defined order.
     */
    public Map<Integer, List<EventBaseDTO>> getRecentEventsByYearOrderedByEndDate() {
        return recentEventsByYearOrderedByEndDate;
    }

    public List<EventBaseDTO> getUpcomingEvents() {
        return upcomingEvents;
    }

    public static Comparator<EventBaseDTO> EVENTS_BY_ASCENDING_DATE_COMPARATOR = new Comparator<EventBaseDTO>() {
        @Override
        public int compare(EventBaseDTO e1, EventBaseDTO e2) {
            return e1.endDate.compareTo(e2.endDate);
        }
    };

    public static Comparator<EventBaseDTO> EVENTS_BY_DESCENDING_DATE_COMPARATOR = new Comparator<EventBaseDTO>() {
        @Override
        public int compare(EventBaseDTO e1, EventBaseDTO e2) {
            return e2.endDate.compareTo(e1.endDate);
        }
    };
}
