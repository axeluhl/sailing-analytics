package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.server.RacingEventService;

public class EventListDataCalculator implements EventVisitor {
    
    private final Map<String, EventListEventDTO> lastestEventPerSeries = new HashMap<>();
    private final EventListViewDTO result = new EventListViewDTO();
    private final RacingEventService service;

    public EventListDataCalculator(RacingEventService service) {
        this.service = service;
    }

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        EventListEventDTO eventDTO = HomeServiceUtil.convertToEventListDTO(event, baseURL, onRemoteServer, service);
        if (HomeServiceUtil.calculateEventState(event) != EventState.UPCOMING && HomeServiceUtil.isFakeSeries(event)) {
            String seriesName = HomeServiceUtil.getSeriesName(event);
            EventListEventDTO latestEvent = lastestEventPerSeries.get(seriesName);
            if (latestEvent == null || latestEvent.getStartDate().before(eventDTO.getStartDate())) {
                lastestEventPerSeries.put(seriesName, eventDTO);
            }
        } else {
            result.addEvent(eventDTO, getYear(eventDTO.getStartDate()));
        }
    }
    
    public EventListViewDTO getResult() {
        for (EventListEventDTO eventDTO : lastestEventPerSeries.values()) {
            result.addEvent(eventDTO, getYear(eventDTO.getStartDate()));
        }
        return result;
    }
    
    private int getYear(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

}
