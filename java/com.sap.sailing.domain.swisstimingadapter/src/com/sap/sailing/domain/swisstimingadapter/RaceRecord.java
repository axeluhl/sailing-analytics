package com.sap.sailing.domain.swisstimingadapter;

import java.util.Date;

public class RaceRecord {

    private String raceID;
    private String description;
    private Date startTime;

    public RaceRecord(String raceID, String description, Date startTime) {
        this.raceID = raceID;
        this.description = description;
        this.startTime = startTime;
    }

    public String getRaceID() {
        return raceID;
    }

    public String getDescription() {
        return description;
    }

    public Date getStartTime() {
        return startTime;
    }

}
