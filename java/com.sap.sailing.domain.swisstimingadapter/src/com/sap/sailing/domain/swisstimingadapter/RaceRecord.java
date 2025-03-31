package com.sap.sailing.domain.swisstimingadapter;

import java.util.Date;

public class RaceRecord {

    private String raceID;
    private String description;
    private Date startTime;
    private boolean hasCourse;
    private boolean hasStartlist;

    public RaceRecord(String raceID, String description, Date startTime, boolean hasCourse, boolean hasStartlist) {
        this.raceID = raceID;
        this.description = description;
        this.startTime = startTime;
        this.hasCourse = hasCourse;
        this.hasStartlist = hasStartlist;
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

    public boolean hasCourse() {
        return hasCourse;
    }

    public boolean hasStartlist() {
        return hasStartlist;
    }
}
