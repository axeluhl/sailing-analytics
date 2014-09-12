package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorResultsDTO implements IsSerializable {

    private Date startTime;
    private RaceMapDataDTO raceCourse;
    private WindFieldDTO windField;
    private PathDTO[] paths;
    private String notificationMessage;

    public SimulatorResultsDTO(){
        this.startTime = null;
        this.raceCourse = null;
        this.windField = null;
        this.paths = null;
        this.notificationMessage = "";
    }

    public SimulatorResultsDTO(final Date startTime, final RaceMapDataDTO raceCourse, final PathDTO[] paths, final WindFieldDTO windField, final String notificationMessage) {
        this.startTime = startTime;
        this.raceCourse = raceCourse;
        this.paths = paths;
        this.windField = windField;
        this.notificationMessage = notificationMessage;
    }
    
    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    
    public RaceMapDataDTO getRaceCourse() {
        return this.raceCourse;
    }

    public void setRaceCourse(final RaceMapDataDTO raceCourse) {
        this.raceCourse = raceCourse;
    }

    public WindFieldDTO getWindField() {
        return this.windField;
    }

    public void setWindField(final WindFieldDTO windField) {
        this.windField = windField;
    }

    public PathDTO[] getPaths() {
        return this.paths;
    }

    public void setPaths(final PathDTO[] paths) {
        this.paths = paths;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(final String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}
