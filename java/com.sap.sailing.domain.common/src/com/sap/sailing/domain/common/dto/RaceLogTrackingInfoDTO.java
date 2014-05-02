package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;

public class RaceLogTrackingInfoDTO implements Serializable {
    private static final long serialVersionUID = -6861382173074620168L;
    public boolean raceLogTrackerExists;
    public boolean competitorRegistrationsExists;
    public RaceLogTrackingState raceLogTrackingState;
    
    protected RaceLogTrackingInfoDTO() {}
    
    public RaceLogTrackingInfoDTO(boolean raceLogTrackerExists, boolean competitorRegistrationsExists,
            RaceLogTrackingState raceLogTrackingState) {
        this.raceLogTrackerExists = raceLogTrackerExists;
        this.competitorRegistrationsExists = competitorRegistrationsExists;
        this.raceLogTrackingState = raceLogTrackingState;
    }
}
