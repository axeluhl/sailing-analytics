package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;

public class RaceLogTrackingInfoDTO implements Serializable {
    private static final long serialVersionUID = -6861382173074620168L;
    public boolean raceLogTrackerExists;
    public boolean competitorRegistrationsExists;
    public boolean courseExists;
    public RaceLogTrackingState raceLogTrackingState;
    
    protected RaceLogTrackingInfoDTO() {}
    
    public RaceLogTrackingInfoDTO(boolean raceLogTrackerExists, boolean competitorRegistrationsExists, boolean courseExists,
            RaceLogTrackingState raceLogTrackingState) {
        this.raceLogTrackerExists = raceLogTrackerExists;
        this.competitorRegistrationsExists = competitorRegistrationsExists;
        this.courseExists = courseExists;
        this.raceLogTrackingState = raceLogTrackingState;
    }
}
