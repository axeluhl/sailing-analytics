package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.List;

import com.sap.sse.common.TimePoint;

class ManeuverDetectionResult {
    
    private final TimePoint latestFixTimePoint;
    private final List<ManeuverSpot> maneuverSpots;

    public ManeuverDetectionResult(TimePoint latestFixTimePoint, List<ManeuverSpot> maneuverSpots) {
        this.latestFixTimePoint = latestFixTimePoint;
        this.maneuverSpots = maneuverSpots;
    }
    
    public TimePoint getLatestRawFixTimePoint() {
        return latestFixTimePoint;
    }
    
    public List<ManeuverSpot> getManeuverSpots() {
        return maneuverSpots;
    }

}
