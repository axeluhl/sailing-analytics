package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindDirectionCandidatesForManeuver {

    private final Maneuver maneuver;
    private final Iterable<WindWithConfidence<TimePoint>> windDirectionCandidates;

    public WindDirectionCandidatesForManeuver(Maneuver maneuver, Iterable<WindWithConfidence<TimePoint>> windDirectionCandidatesForSameTimePoint) {
        this.maneuver = maneuver;
        this.windDirectionCandidates = windDirectionCandidatesForSameTimePoint;
    }
    
    public TimePoint getTimePoint() {
        return maneuver.getTimePoint();
    }
    
    public Iterable<WindWithConfidence<TimePoint>> getWindDirectionCandidates() {
        return windDirectionCandidates;
    }
    
    public Maneuver getManeuver() {
        return maneuver;
    }

}
