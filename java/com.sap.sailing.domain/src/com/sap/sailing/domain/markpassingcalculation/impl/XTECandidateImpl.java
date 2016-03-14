package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public class XTECandidateImpl extends CandidateWithSide {
    private final Boolean passesInTheRightDirection;

    public XTECandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability,
            Double startProbabilityBasedOnOtherCompetitors, Waypoint w, boolean onCorrectSideOfWaypoint,
            Boolean passesInTheRightDirection) {
        super(oneBasedIndexOfWaypoint, p, probability, startProbabilityBasedOnOtherCompetitors, w, onCorrectSideOfWaypoint);
        this.passesInTheRightDirection = passesInTheRightDirection;
    }

    @Override
    public String toString() {
        return super.toString()+", passesInTheRightDirection: "+passesInTheRightDirection;
    }
}
