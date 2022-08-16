package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

public class CandidateForFixedMarkPassingImpl extends CandidateImpl {
    private static final long serialVersionUID = 3657502693032556232L;

    public CandidateForFixedMarkPassingImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Waypoint w) {
        super(oneBasedIndexOfWaypoint, p, probability, w);
    }

    @Override
    public boolean isFixed() {
        return true;
    }
}
