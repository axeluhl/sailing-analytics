package com.sap.sailing.domain.markpassingcalculation.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;

/**
 * A possible passing of a {@link Waypoint}. It contains the {@link Waypoint} that it might be passing, a
 * {@link TimePoint}, the probability that this candidate is a passing (e.g. based on the distance to the
 * {@link Waypoint}) and the one-based(!) ID of this Waypoint. The ID is one based because the standard implemantation
 * of {@link CandidateChooser} (see {@link CandidateChooserImpl}) uses a proxy Candidates at the end and the
 * beginning of the race, the one at the beginning recieves the ID 0. Candidates are created in an
 * {@link CandidateFinder}, which determines where a passing could be and assigns them
 * 
 * @author Nicolas Klose
 * 
 */

public class CandidateImpl implements Candidate {
    private final Waypoint w;
    private final TimePoint p;
    private final double distanceBasedProbability;
    private final Integer oneBasedIndexOfWaypoint;
    // TODO Document what side and direction mean
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
