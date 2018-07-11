package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.home.communication.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.util.EventUtil;

public class EventListDataCalculator implements EventVisitor {
    
    private final Map<LeaderboardGroupBase, EventListEventDTO> lastestEventPerSeries = new HashMap<>();
    private final Map<LeaderboardGroupBase, Integer> numberOfEventsPerSeries = new HashMap<>();
    private final EventListViewDTO result = new EventListViewDTO();
    private final RacingEventService service;

    public EventListDataCalculator(RacingEventService service) {
        this.service = service;
    }

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        if (event.getStartDate() != null) {
            EventListEventDTO eventDTO = HomeServiceUtil.convertToEventListDTO(event, baseURL, onRemoteServer, service);
            if (HomeServiceUtil.calculateEventState(event) != EventState.UPCOMING && EventUtil.isFakeSeries(event)) {
                final LeaderboardGroupBase seriesLeaderboardGroup = event.getLeaderboardGroups().iterator().next();
                EventListEventDTO latestEvent = lastestEventPerSeries.get(seriesLeaderboardGroup);
                if (latestEvent == null || latestEvent.getStartDate().before(eventDTO.getStartDate())) {
                    lastestEventPerSeries.put(seriesLeaderboardGroup, eventDTO);
                }
                increaseNumberOfEvents(seriesLeaderboardGroup);
            } else {
                addEventToResults(eventDTO);
            }
        }
    }
    
    private void increaseNumberOfEvents(final LeaderboardGroupBase seriesLeaderboardGroup) {
        Integer currentValue = numberOfEventsPerSeries.get(seriesLeaderboardGroup);
        currentValue = currentValue == null ? 0 : currentValue;
        numberOfEventsPerSeries.put(seriesLeaderboardGroup, ++currentValue);
    }
    
    public EventListViewDTO getResult() {
        for (Entry<LeaderboardGroupBase, EventListEventDTO> latestEventInSeries : lastestEventPerSeries.entrySet()) {
            final LeaderboardGroupBase seriesLeaderboardGroup = latestEventInSeries.getKey();
            EventListEventDTO latestEvent = latestEventInSeries.getValue();
            latestEvent.setEventSeries(new EventListEventSeriesDTO(
                    HomeServiceUtil.getLeaderboardDisplayName(seriesLeaderboardGroup), seriesLeaderboardGroup.getId()));
            latestEvent.getEventSeries().setEventsCount(numberOfEventsPerSeries.get(seriesLeaderboardGroup));
            addEventToResults(latestEvent);
        }
        result.addStatistics(service.getOverallStatisticsByYear());
        return result;
    }
    
    private void addEventToResults(EventListEventDTO eventDTO) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(eventDTO.getStartDate());
        result.addEvent(eventDTO, cal.get(Calendar.YEAR));
    }

}
