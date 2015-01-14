package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sse.common.TimePoint;

public class CandidateImpl implements Candidate {
    private final Waypoint w;
    protected TimePoint p;
    private final double probability;
    private final Integer oneBasedIndexOfWaypoint;

    public CandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double probability, Waypoint w) {
        this.w = w;
        this.p = p;
        this.probability = probability;
        this.oneBasedIndexOfWaypoint = oneBasedIndexOfWaypoint;

    }

    @Override
    public int getOneBasedIndexOfWaypoint() {
        return oneBasedIndexOfWaypoint;
    }

    @Override
    public TimePoint getTimePoint() {
        return p;
    }

    @Override
    public Double getProbability() {

        return probability;
    }

    @Override
    public Waypoint getWaypoint() {
        return w;
    }

    public String toString() {
        return "Candidate for waypoint" + oneBasedIndexOfWaypoint + " with cost " + getProbability() + "and Timepoint " + p;
    }

    @Override
    public int compareTo(CandidateImpl arg0) {
        return compareTo((Candidate) arg0);
    }

    @Override
    public int compareTo(Candidate arg0) {
        return oneBasedIndexOfWaypoint != arg0.getOneBasedIndexOfWaypoint() ? oneBasedIndexOfWaypoint.compareTo(arg0.getOneBasedIndexOfWaypoint()) : p != arg0.getTimePoint() ? p.compareTo(arg0.getTimePoint()) : getProbability().compareTo(arg0.getProbability());
    }
}
