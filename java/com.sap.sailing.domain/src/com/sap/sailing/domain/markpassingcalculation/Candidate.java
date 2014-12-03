package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.TimePoint;

/**
 * A possible passing of a {@link Waypoint}. It contains the {@link Waypoint} that it might be passing, a
 * {@link TimePoint}, the probability that this candidate is a passing (e.g. based on the distance to the
 * {@link Waypoint}) and the one-based(!) ID of this Waypoint. The ID is one based because the standard implementation
 * of {@link AbstractCandidateChooser} (see {@link CandidateChooser}) uses a proxy Candidates at the end and the
 * beginning of the race, the one at the beginning receives the ID 0. Candidates are created in an
 * {@link AbstractCandidateFinder}, which determines where a passing could be and assigns them
 * 
 * @author Nicolas Klose
 * 
 */
public class Candidate implements Comparable<Candidate> {
    private Waypoint waypoint;
    private TimePoint timePoint;
    private double distanceProbability;
    private Integer id;
    private boolean rightSide;
    private boolean rightDirection;
    private String type;

    public Candidate(int id, TimePoint timePoint, double distanceProbability, Waypoint w, boolean rightSide, boolean rightDirection, String type) {
        this.waypoint = w;
        this.timePoint = timePoint;
        this.distanceProbability = distanceProbability;
        this.id = id;
        this.rightSide = rightSide;
        this.rightDirection=rightDirection;
        this.type = type;
    }

    public int getID() {
        return id;
    }

    public TimePoint getTimePoint() {
        return timePoint;
    }

    public Double getProbability() {
        double factor= (rightSide&&rightDirection)?1:(rightSide||rightDirection)?0.7:0.4;
        double cost = distanceProbability * factor;
        return cost;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public String toString() {
        return type + "-Candidate for " + id + " with cost " + getProbability() + "and Timepoint " + timePoint;
    }

    @Override
    public int compareTo(Candidate arg0) {
        return id != arg0.getID() ? id.compareTo(arg0.getID()) : timePoint != arg0.getTimePoint() ? timePoint.compareTo(arg0.getTimePoint()) : getProbability().compareTo(arg0.getProbability());
    }
}
