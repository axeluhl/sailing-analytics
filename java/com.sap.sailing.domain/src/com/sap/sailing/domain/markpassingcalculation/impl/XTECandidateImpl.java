package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public class XTECandidateImpl extends CandidateWithSide {
    private static final long serialVersionUID = 700807010904741849L;
    private final Double passesInTheRightDirectionProbability;

    public XTECandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability,
            Double startProbabilityBasedOnOtherCompetitors, Waypoint w, double onCorrectSideOfWaypoint,
            Double passesInTheRightDirectionProbability) {
        super(oneBasedIndexOfWaypoint, p, probability, startProbabilityBasedOnOtherCompetitors, w, onCorrectSideOfWaypoint);
        this.passesInTheRightDirectionProbability = passesInTheRightDirectionProbability;
    }

    @Override
    public String toString() {
        return super.toString()+", passesInTheRightDirection: "+passesInTheRightDirectionProbability;
    }
}
