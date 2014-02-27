package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateImpl;

public interface Candidate extends Comparable<CandidateImpl> {
    
    Waypoint getWaypoint();

    int getOneBasedIndexOfWaypoint();

    TimePoint getTimePoint();

    Double getProbability();

    int compareTo(Candidate arg0);
}
