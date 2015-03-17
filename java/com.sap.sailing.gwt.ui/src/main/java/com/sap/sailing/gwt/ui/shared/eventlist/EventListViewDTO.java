package com.sap.sailing.gwt.ui.shared.eventlist;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class EventListViewDTO implements IsSerializable {
    
    private ArrayList<EventListEventDTO> upcommingEvents = new ArrayList<>();
    private ArrayList<EventListYearDTO> recentEvents = new ArrayList<>();
    
    public ArrayList<EventListEventDTO> getUpcommingEvents() {
        return upcommingEvents;
    }

    private void addUpcommingEvent(EventListEventDTO event) {
        for(int i = 0; i < upcommingEvents.size(); i++) {
            if(upcommingEvents.get(i).getStartDate().compareTo(event.getStartDate()) > 0) {
                upcommingEvents.add(i, event);
                return;
            }
        }
        upcommingEvents.add(event);
    }
    
    public ArrayList<EventListYearDTO> getRecentEvents() {
        return recentEvents;
    }

    private  EventListYearDTO getYear(int year) {
        for(int i = 0; i < recentEvents.size(); i++) {
            EventListYearDTO yearDTO = recentEvents.get(i);
            if(year == yearDTO.getYear()) {
                return yearDTO;
            }
            if(year > yearDTO.getYear()) {
                EventListYearDTO newYear = new EventListYearDTO(year);
                recentEvents.add(i, newYear);
                return newYear;
            }
        }
        EventListYearDTO newYear = new EventListYearDTO(year);
        recentEvents.add(newYear);
        return newYear;
    }

    public void addEvent(EventListEventDTO event, int year) {
        if (event.getState() == EventState.RUNNING || event.getState() == EventState.FINISHED) {
            // recent event or live event
            getYear(year).addEvent(event);
        } else {
            // upcoming event
            addUpcommingEvent(event);
        }
    }
}
