package com.sap.sailing.gwt.ui.shared.race;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RaceMetadataDTO implements IsSerializable {
    
    public enum RaceViewState {
        PLANNED {       // no start time set
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsPlanned();
            }
        },
        SCHEDULED {     // the start time is set and in the future
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsScheduled(); // fallback representation
            }
        },
        RUNNING {       // start time is the past and end time is not set or in the future
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsRunning(); // fallback representation
            }
        },
        FINISHED {      // the end time is set and is in the past
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsFinished();
            }
        },
        POSTPONED {     // the start has been postponed
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsPostponed();
            }
        },
        CANCELED {      // the running racing has been canceled
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsCanceled();
            }
        };

        public abstract String getLabel();
    }
    
    public enum RaceTrackingState {
        NOT_TRACKED,             // No tracking data -> probably just managed by race committee app
        TRACKED_NO_VALID_DATA,   // tracking is connected but the required data for displaying the race viewer is not available
        TRACKED_VALID_DATA       // tracking is connected and all required data for displaying the race viewer is available
    }
    
    private String regattaName;
    private String regattaDisplayName;
    private String raceName;
    private String trackedRaceName;
    private FleetMetadataDTO fleet;
    private Date start;
    private String courseArea;
    private String course;
    private String boatClass;
    private RaceViewState state;
    private RaceTrackingState trackingState;

    protected RaceMetadataDTO() {
    }
    
    public RaceMetadataDTO(String regattaName, String raceName) {
        this.regattaName = regattaName;
        this.raceName = raceName;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getRaceName() {
        return raceName;
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

    public String getTrackedRaceName() {
        return trackedRaceName;
    }

    public void setTrackedRaceName(String trackedRaceName) {
        this.trackedRaceName = trackedRaceName;
    }

    public String getRegattaDisplayName() {
        return regattaDisplayName;
    }

    public void setRegattaDisplayName(String regattaDisplayName) {
        this.regattaDisplayName = regattaDisplayName;
    }
}
