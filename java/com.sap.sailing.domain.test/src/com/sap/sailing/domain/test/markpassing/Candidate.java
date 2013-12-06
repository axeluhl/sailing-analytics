package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;

/**
 * A possible passing of a Waypoint. It contains the Waypoint it might be passing, the ID of this Waypoint, a Timepoint
 * and the proability that this candidate is a passing (e.g. based on the distance to the mark).
 * 
 * @author Nicolas Klose
 * 
 */

public class Candidate {
    private Waypoint w;
    private TimePoint p;
    private double distanceProbability;
    private int id;

    public Candidate(int id, TimePoint p, double distanceProbability, Waypoint w) {
        this.w = w;
        this.p = p;
        this.distanceProbability = distanceProbability;
        this.id = id;
    }

    public Candidate(int id, TimePoint p, double distanceProbability) {
        this.id = id;
        this.p = p;
        this.distanceProbability = distanceProbability;
    }

    public int getID() {
        return id;
    }

    public TimePoint getTimePoint() {
        return p;
    }

    public double getProbability() {
        return distanceProbability;
    }

    public Waypoint getWaypoint() {
        return w;
    }
}
