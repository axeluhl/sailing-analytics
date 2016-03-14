package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sse.common.TimePoint;

public class DistanceCandidateImpl extends CandidateWithSide {
    private final Distance distance;
    
    public DistanceCandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Double startProbabilityBasedOnOtherCompetitors,
            Waypoint w, boolean onCorrectSideOfWaypoint, Distance distance) {
        super(oneBasedIndexOfWaypoint, p, probability, startProbabilityBasedOnOtherCompetitors, w, onCorrectSideOfWaypoint);
        this.distance = distance;
    }

    @Override
    public String toString() {
        return super.toString()+", distance: "+distance;
    }
}
