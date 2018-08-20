package com.sap.sailing.manage2sail;

import java.util.Date;

public class RaceResultDescriptor {
    private String id;
    private String name;
    private Integer raceColumnNumber;
    private String seriesName;
    private String fleetName;
    private Boolean isTracked;
    private Date startTime;

    /** Possible states: Scheduled, StartSequence, Racing, Finished, GeneralRecall, Postponed, Abandoned */
    private String status;

    public Integer getRaceColumnNumber() {
        return raceColumnNumber;
    }

    public void setRaceColumnNumber(Integer raceColumnNumber) {
        this.raceColumnNumber = raceColumnNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isTracked() {
        return isTracked;
    }

    public void setTracked(Boolean isTracked) {
        this.isTracked = isTracked;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
