package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sse.common.TimePoint;

public class TrackTimeInfo {
    
    private final TimePoint trackStartTimePoint;
    private final TimePoint trackEndTimePoint;
    private final TimePoint latestRawFixTimePoint;
    
    public TrackTimeInfo(TimePoint trackStartTimePoint, TimePoint trackEndTimePoint, TimePoint latestRawFixTimePoint) {
        this.trackStartTimePoint = trackStartTimePoint;
        this.trackEndTimePoint = trackEndTimePoint;
        this.latestRawFixTimePoint = latestRawFixTimePoint;
    }

    public TimePoint getTrackStartTimePoint() {
        return trackStartTimePoint;
    }

    public TimePoint getTrackEndTimePoint() {
        return trackEndTimePoint;
    }

    public TimePoint getLatestRawFixTimePoint() {
        return latestRawFixTimePoint;
    }

}
