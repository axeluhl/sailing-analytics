package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

public class DistanceCandidateImpl extends CandidateWithSide {
    private static final long serialVersionUID = -9125694086262205898L;
    private final Distance distance;
    
    public DistanceCandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Double startProbabilityBasedOnOtherCompetitors,
            Waypoint w, double onCorrectSideOfWaypoint, Distance distance) {
        super(oneBasedIndexOfWaypoint, p, probability, startProbabilityBasedOnOtherCompetitors, w, onCorrectSideOfWaypoint);
        this.distance = distance;
    }

    @Override
    public String toString() {
        return super.toString()+", distance: "+distance;
    }
}
