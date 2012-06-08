package com.sap.sailing.kiworesultimport.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.RaceSummary;
import com.sap.sailing.kiworesultimport.RegattaSummary;

public class RegattaSummaryImpl implements RegattaSummary {
    private final Iterable<RaceSummary> races;
    private final String boatClassName;
    private final Iterable<Boat> boats;
    private final TimePoint timePointPublished;
    
    public RegattaSummaryImpl(Iterable<RaceSummary> races, String boatClassName, Iterable<Boat> boats, TimePoint timePointPublished) {
        super();
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
        return races;
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

}
