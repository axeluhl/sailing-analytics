package com.sap.sailing.gwt.ui.shared.race;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class RaceMetadataDTO implements IsSerializable {
    
    public enum RaceViewState {
        PLANNED,        // no start time set
        SCHEDULED,      // the start time is set and in the future 
        RUNNING,        // start time is the past and end time is not set or in the future
        FINISHED,       // the end time is set and is in the past
        POSTPONED,      // the start has been postponed
        CANCELED        // the running racing has been canceled
    }
    
    public enum RaceTrackingState {
        NOT_TRACKED,             // No tracking data -> probably just managed by race committee app
        TRACKED_NO_VALID_DATA,   // tracking is connected but the required data for displaying the race viewer is not available
        TRACKED_VALID_DATA       // tracking is connected and all required data for displaying the race viewer is available
    }
    
    private RegattaAndRaceIdentifier id;

    private String raceName;
    private FleetMetadataDTO fleet;
    private Date start;
    private String courseArea;
    private String course;
    private String boatClass;
    private RaceViewState state;
    private RaceTrackingState trackingState;

    protected RaceMetadataDTO() {
    }
    
    public RaceMetadataDTO(RegattaAndRaceIdentifier id) {
        this.id = id;
    }
    
    public RegattaAndRaceIdentifier getID() {
        return id;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public FleetMetadataDTO getFleet() {
        return fleet;
    }

    public void setFleet(FleetMetadataDTO fleet) {
        this.fleet = fleet;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getCourseArea() {
        return courseArea;
    }

    public void setCourseArea(String courseArea) {
        this.courseArea = courseArea;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    public RaceViewState getViewState() {
        return state;
    }

    public void setViewState(RaceViewState state) {
        this.state = state;
    }

    public RaceTrackingState getTrackingState() {
        return trackingState;
    }

    public void setTrackingState(RaceTrackingState trackingState) {
        this.trackingState = trackingState;
    }
}
