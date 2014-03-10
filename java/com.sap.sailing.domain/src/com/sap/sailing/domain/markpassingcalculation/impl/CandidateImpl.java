package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;

public class CandidateImpl implements Candidate {
    private final Waypoint w;
    private final TimePoint p;
    private final double distanceBasedProbability;
    private final Integer oneBasedIndexOfWaypoint;
    // TODO Interface + no side and direction
    private final boolean correctSide;
    private final boolean correctDirection;
    private final String type;

    public CandidateImpl(int oneBasedIndexOfWaypoint, TimePoint p, double distanceProbability, Waypoint w, boolean rightSide, boolean rightDirection, String type) {
        this.w = w;
        this.p = p;
        this.distanceBasedProbability = distanceProbability;
        this.oneBasedIndexOfWaypoint = oneBasedIndexOfWaypoint;
        this.correctSide = rightSide;
        this.correctDirection = rightDirection;
        this.type = type;
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
        double factor= (correctSide&&correctDirection)?1:(correctSide||correctDirection)?0.7:0.4;
        double cost = distanceBasedProbability * factor;
        return cost;
    }

    @Override
    public Waypoint getWaypoint() {
        return w;
    }

    public String toString() {
        return type + "-Candidate for " + oneBasedIndexOfWaypoint + " with cost " + getProbability() + "and Timepoint " + p;
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
