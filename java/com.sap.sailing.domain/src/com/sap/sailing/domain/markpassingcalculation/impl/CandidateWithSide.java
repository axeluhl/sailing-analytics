package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public abstract class CandidateWithSide extends CandidateImpl {
    private static final long serialVersionUID = -1670977291984975178L;
    private final double onCorrectSideOfWaypoint;
    private final Double startProbabilityBasedOnOtherCompetitors;

    public CandidateWithSide(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Double startProbabilityBasedOnOtherCompetitors,
            Waypoint w, double onCorrectSideOfWaypoint) {
        super(oneBasedIndexOfWaypoint, p, probability, w);
        this.startProbabilityBasedOnOtherCompetitors = startProbabilityBasedOnOtherCompetitors;
        this.onCorrectSideOfWaypoint = onCorrectSideOfWaypoint;
    }
    
    @Override
    public String toString() {
        return ""+getClass().getSimpleName()+" "+super.toString()+", onCorrectSideOfWaypoint: "+onCorrectSideOfWaypoint+
                (startProbabilityBasedOnOtherCompetitors==null?"":", startProbabilityBasedOnOtherCompetitors="+startProbabilityBasedOnOtherCompetitors);
    }
}
