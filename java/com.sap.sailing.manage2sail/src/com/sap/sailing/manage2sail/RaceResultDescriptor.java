package com.sap.sailing.manage2sail;

import java.util.Date;

public class RaceResultDescriptor {
    private String id;
    private String name;
    private Date startTimeUTC;
    private Date endTimeUTC;
    private String state;
    private Boolean isTracked;
    
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
    public Date getStartTimeUTC() {
        return startTimeUTC;
    }
    public void setStartTimeUTC(Date startTimeUTC) {
        this.startTimeUTC = startTimeUTC;
    }
    public Date getEndTimeUTC() {
        return endTimeUTC;
    }
    public void setEndTimeUTC(Date endTimeUTC) {
        this.endTimeUTC = endTimeUTC;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Boolean isTracked() {
        return isTracked;
    }
    public void setTracked(Boolean isTracked) {
        this.isTracked = isTracked;
    }
}
