package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public class XTECandidateImpl extends CandidateWithSide {
    private final boolean passesInTheRightDirection;

    public XTECandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Waypoint w, double onCorrectSideOfWaypoint, boolean passesInTheRightDirection) {
        super(oneBasedIndexOfWaypoint, p, probability, w, onCorrectSideOfWaypoint);
        this.passesInTheRightDirection = passesInTheRightDirection;
    }

    @Override
    public String toString() {
        return super.toString()+", passesInTheRightDirection: "+passesInTheRightDirection;
    }
}
