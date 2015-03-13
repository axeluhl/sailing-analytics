package com.sap.sailing.gwt.ui.shared.eventlist;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventListYearDTO implements IsSerializable {
    private int year;
    private int countryCount;
    private int sailorCount;
    private int trackedRacesCount;
    private ArrayList<EventListEventDTO> events = new ArrayList<>();
    
    @SuppressWarnings("unused")
    private EventListYearDTO() {
    }
    
    public EventListYearDTO(int year) {
        this.year = year;
    }
    
    public int getYear() {
        return year;
    }

    public List<EventListEventDTO> getEvents() {
        return events;
    }

    protected void addEvent(EventListEventDTO event) {
        for(int i = 0; i < events.size(); i++) {
            if(events.get(i).getStartDate().compareTo(event.getStartDate()) < 0) {
                events.add(i, event);
                return;
            }
        }
        events.add(event);
    }

    public int getCountryCount() {
        return countryCount;
    }

    public void setCountryCount(int countryCount) {
        this.countryCount = countryCount;
    }

    public int getSailorCount() {
        return sailorCount;
    }

    public void setSailorCount(int sailorCount) {
        this.sailorCount = sailorCount;
    }

    public int getTrackedRacesCount() {
        return trackedRacesCount;
    }

    public void setTrackedRacesCount(int trackedRacesCount) {
        this.trackedRacesCount = trackedRacesCount;
    }
}
