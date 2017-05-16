package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.Date;

public class SwissTimingReplayRaceDTO extends AbstractRaceRecordDTO {
    public String flight_number;
    public String race_id;
    public String rsc;
    public String boat_class;
    public Date startTime;
    public String link;

    public SwissTimingReplayRaceDTO() {}
    
    public SwissTimingReplayRaceDTO(String flight_number, String race_id, String rsc, String name, String boat_class, Date startTime, String link, boolean hasRememberedRegatta) {
        super(name, hasRememberedRegatta);
        this.flight_number = flight_number;
        this.race_id = race_id;
        this.rsc = rsc;
        this.boat_class = boat_class;
        this.startTime = startTime;
        this.link = link;
    }
    
    @Override
    public Iterable<String> getBoatClassNames() {
        return Collections.singleton(boat_class);
    }

    @Override
    public String toString() {
        return "Race " + getName() + " - " + boat_class + " (" + startTime + ", flight " + flight_number + ')';
    }
}
