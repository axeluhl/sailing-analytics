package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.RaceSummary;
import com.sap.sailing.kiworesultimport.RegattaSummary;
import com.sap.sse.common.TimePoint;

public class RegattaSummaryImpl implements RegattaSummary {
    private final Iterable<RaceSummary> races;
    private final String boatClassName;
    private final Iterable<Boat> boats;
    private final TimePoint timePointPublished;
    private final String event;
    
    public RegattaSummaryImpl(String event, Iterable<RaceSummary> races, String boatClassName, Iterable<Boat> boats,
            TimePoint timePointPublished) {
        super();
        this.event = event;
        this.races = races;
        this.boatClassName = boatClassName;
        this.boats = boats;
        this.timePointPublished = timePointPublished;
    }

    @Override
    public TimePoint getTimePointPublished() {
        return timePointPublished;
    }

    @Override
    public String getBoatClassName() {
        return boatClassName;
    }

    @Override
    public Iterable<RaceSummary> getRaces() {
        List<RaceSummary> result = new ArrayList<RaceSummary>();
        for (RaceSummary race : races) {
            result.add(race);
        }
        Collections.sort(result, new Comparator<RaceSummary>() {
            @Override
            public int compare(RaceSummary o1, RaceSummary o2) {
                return o1.getRaceNumber() - o2.getRaceNumber();
            }
        });
        return result;
    }

    @Override
    public RaceSummary getRace(int raceNumberOneBased) {
        for (RaceSummary race : getRaces()) {
            if (race.getRaceNumber() == raceNumberOneBased) {
                return race;
            }
        }
        return null;
    }

    @Override
    public Iterable<Boat> getBoats() {
        return boats;
    }

    @Override
    public String getEventName() {
        return event;
    }

}
