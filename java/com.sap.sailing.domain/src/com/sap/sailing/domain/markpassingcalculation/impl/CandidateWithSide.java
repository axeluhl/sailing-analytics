package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public abstract class CandidateWithSide extends CandidateImpl {
    private final boolean onCorrectSideOfWaypoint;

    public CandidateWithSide(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Waypoint w,
            boolean onCorrectSideOfWaypoint) {
        super(oneBasedIndexOfWaypoint, p, probability, w);
        this.onCorrectSideOfWaypoint = onCorrectSideOfWaypoint;
    }
    
    @Override
    public String toString() {
        return ""+getClass().getSimpleName()+" "+super.toString()+", onCorrectSideOfWaypoint: "+onCorrectSideOfWaypoint;
    }
}
