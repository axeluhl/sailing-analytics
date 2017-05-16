package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.Date;

import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sse.common.impl.NamedImpl;

public class SwissTimingReplayRaceImpl extends NamedImpl implements SwissTimingReplayRace {
    private static final long serialVersionUID = -8863753739203404760L;
    private final String jsonurl;
    private final String flight_number;
    private final String race_id;
    private final String rsc;
    private final String boat_class;
    private final Date startTime;
    private final String link;

    public SwissTimingReplayRaceImpl(String jsonurl, String flight_number, String race_id, String rsc, String name, String boat_class, Date startTime, String link) {
        super(name);
        this.jsonurl = jsonurl;
        this.flight_number = flight_number;
        this.race_id = race_id;
        this.rsc = rsc;
        this.boat_class = boat_class;
        this.startTime = startTime;
        this.link = link;
    }
    
    @Override
    public String toString() {
        return "Race " + getName() + " - " + getBoatClass() + " (" + getStartTime() + ", flight " + getFlightNumber() + ')';
    }

    @Override
    public String getJsonUrl() {
        return jsonurl;
    }

    @Override
    public String getFlightNumber() {
        return flight_number;
    }

    @Override
    public String getRaceId() {
        return race_id;
    }

    @Override
    public String getRsc() {
        return rsc;
    }

    @Override
    public String getBoatClass() {
        return boat_class;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public String getLink() {
        return link;
    }
}
