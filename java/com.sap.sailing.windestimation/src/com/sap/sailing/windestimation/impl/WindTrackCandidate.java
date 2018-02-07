package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;

public class WindTrackCandidate {
    
    private final double trackConfidence;
    private final Iterable<WindWithConfidence<TimePoint>> windTrack;

    public WindTrackCandidate(double trackConfidence, Iterable<WindWithConfidence<TimePoint>> windTrack) {
        this.trackConfidence = trackConfidence;
        this.windTrack = windTrack;
    }
    
    public double getTrackConfidence() {
        return trackConfidence;
    }
    
    public Iterable<WindWithConfidence<TimePoint>> getWindTrack() {
        return windTrack;
    }

}
