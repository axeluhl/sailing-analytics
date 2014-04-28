package com.sap.sailing.domain.common.dto;

import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;

public class RaceLogTrackingInfoDTO {
    public boolean raceLogTrackerExists;
    public boolean competitorRegistrationsExists;
    public RaceLogTrackingState raceLogTrackingState;
    
    public RaceLogTrackingInfoDTO(boolean raceLogTrackerExists, boolean competitorRegistrationsExists,
            RaceLogTrackingState raceLogTrackingState) {
        this.raceLogTrackerExists = raceLogTrackerExists;
        this.competitorRegistrationsExists = competitorRegistrationsExists;
        this.raceLogTrackingState = raceLogTrackingState;
    }
}
