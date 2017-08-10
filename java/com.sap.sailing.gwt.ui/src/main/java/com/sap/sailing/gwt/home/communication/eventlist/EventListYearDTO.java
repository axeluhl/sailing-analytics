package com.sap.sailing.gwt.home.communication.eventlist;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class EventListYearDTO implements IsSerializable {
    private int year;
    private int eventCount;
    private int numberOfCompetitors;
    private int numberOfRegattas;
    private int numberOfRaces;
    private int numberOfTrackedRaces;
    private long numberOfGPSFixes;
    private long numberOfWindFixes;
    private Distance distanceTraveled;
    private SimpleCompetitorDTO fastestCompetitor;
    private Double fastestCompetitorSpeedInKnots;
    
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

    @GwtIncompatible
    protected void addEvent(EventListEventDTO event) {
        eventCount += event.getEventSeries() == null ? 1 : event.getEventSeries().getEventsCount();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getStartDate().compareTo(event.getStartDate()) < 0) {
                events.add(i, event);
                return;
            }
        }
        events.add(event);
    }

    @GwtIncompatible
    protected void addStatistics(Statistics statistics) {
        numberOfCompetitors = statistics.getNumberOfCompetitors();
        numberOfRegattas = statistics.getNumberOfRegattas();
        numberOfRaces = statistics.getNumberOfRaces();
        numberOfTrackedRaces = statistics.getNumberOfTrackedRaces();
        numberOfGPSFixes = statistics.getNumberOfGPSFixes();
        numberOfWindFixes = statistics.getNumberOfWindFixes();
        distanceTraveled = statistics.getDistanceTraveled();
        final Triple<Competitor, Speed, TimePoint> maxSpeed = statistics.getMaxSpeed();
        if (maxSpeed != null) {
            fastestCompetitor = new SimpleCompetitorDTO(maxSpeed.getA());
            fastestCompetitorSpeedInKnots = maxSpeed.getB().getKnots();
        }
    }

    public int getEventCount() {
        return eventCount;
    }

    public int getNumberOfCompetitors() {
        return numberOfCompetitors;
    }

    public int getNumberOfRegattas() {
        return numberOfRegattas;
    }

    public int getNumberOfRaces() {
        return numberOfRaces;
    }

    public int getNumberOfTrackedRaces() {
        return numberOfTrackedRaces;
    }

    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public Distance getDistanceTraveled() {
        return distanceTraveled;
    }
    
    public SimpleCompetitorDTO getFastestCompetitor() {
        return fastestCompetitor;
    }
    
    public Double getFastestCompetitorSpeedInKnots() {
        return fastestCompetitorSpeedInKnots;
    }
}
