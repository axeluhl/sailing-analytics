package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingReplayRaceDTO implements IsSerializable {
    public String flight_number;
    public String race_id;
    public String rsc;
    public String name;
    public String boat_class;
    public Date startTime;
    public String link;
    public boolean hasRememberedRegatta;

    public SwissTimingReplayRaceDTO() {}
    
    public SwissTimingReplayRaceDTO(String flight_number, String race_id, String rsc, String name, String boat_class, Date startTime, String link, boolean hasRememberedRegatta) {
        this.flight_number = flight_number;
        this.race_id = race_id;
        this.rsc = rsc;
        this.name = name;
        this.boat_class = boat_class;
        this.startTime = startTime;
        this.link = link;
        this.hasRememberedRegatta = hasRememberedRegatta;
    }
    
    @Override
    public String toString() {
        return "Race " + name + " - " + boat_class + " (" + startTime + ", flight " + flight_number + ')';
    }
}
