package com.sap.sailing.domain.swisstimingreplayadapter;

import java.util.Date;

public class SwissTimingReplayRace {
    public String jsonurl;
    public String flight_number;
    public String race_id;
    public String rsc;
    public String name;
    public String boat_class;
    public Date startTime;
    public String link;

    public SwissTimingReplayRace() {}
    
    public SwissTimingReplayRace(String jsonurl, String flight_number, String race_id, String rsc, String name, String boat_class, Date startTime, String link) {
        this.jsonurl = jsonurl;
        this.flight_number = flight_number;
        this.race_id = race_id;
        this.rsc = rsc;
        this.name = name;
        this.boat_class = boat_class;
        this.startTime = startTime;
        this.link = link;
    }
    
    @Override
    public String toString() {
        return "Race " + name + " - " + boat_class + " (" + startTime + ", flight " + flight_number + ')';
    }
}
