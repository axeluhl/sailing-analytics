package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sse.common.TimePoint;

public class WindDirectionCandidatesForTimePoint {

    private final TimePoint timePoint;
    private final Iterable<BearingWithConfidence<TimePoint>> windDirectionCandidates;

    public WindDirectionCandidatesForTimePoint(TimePoint timePoint, Iterable<BearingWithConfidence<TimePoint>> windDirectionCandidatesForSameTimePoint) {
        this.timePoint = timePoint;
        this.windDirectionCandidates = windDirectionCandidatesForSameTimePoint;
    }
    
    public TimePoint getTimePoint() {
        return timePoint;
    }
    
    public Iterable<BearingWithConfidence<TimePoint>> getWindDirectionCandidates() {
        return windDirectionCandidates;
    }

}
