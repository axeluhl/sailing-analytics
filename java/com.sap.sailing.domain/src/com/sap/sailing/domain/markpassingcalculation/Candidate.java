package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;

/**
 * A possible passing of a {@link Waypoint}. It contains the {@link Waypoint} that it might be passing, a
 * {@link TimePoint}, the probability that this candidate is a passing (e.g. based on the distance to the
 * {@link Waypoint}) and the one-based(!) ID of this Waypoint. The ID is one based because the standard implemantation
 * of {@link AbstractCandidateChooser} (see {@link CandidateChooser}) uses a proxy Candidates at the end and the
 * beginning of the race, the one at the beginning recieves the ID 0. Candidates are created in an
 * {@link AbstractCandidateFinder}, which determines where a passing could be and assigns them 
 * 
 * @author Nicolas Klose
 * 
 */

public class Candidate  {
    private Waypoint w;
    private TimePoint p;
    private double probability;
    private int id;
    // TODO Boolean for right side of waypoint and right rounding way for waypoint

    public Candidate(int id, TimePoint p, double distanceProbability, Waypoint w) {
        this.w = w;
        this.p = p;
        this.probability = distanceProbability;
        this.id = id;
    }

    public Candidate(int id, TimePoint p, double distanceProbability) {
        this.id = id;
        this.p = p;
        this.probability = distanceProbability;
    }

    public int getID() {
        return id;
    }

    public TimePoint getTimePoint() {
        return p;
    }

    public double getProbability() {
        return probability;
    }

    public Waypoint getWaypoint() {
        return w;
    }
    public String toString(){
        return "Candidate for " + id + " with cost "+ probability + "and Timepoint " + p;
    }
}
