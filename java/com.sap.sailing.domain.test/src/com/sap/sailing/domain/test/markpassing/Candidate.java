package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;

public class Candidate {
    private Waypoint w;
    private TimePoint p;
    private double distanceLikelyhood;
    private int id;

    public Candidate(int id, TimePoint p, double distanceLikelyhood, Waypoint w) {
        this.w = w;
        this.p = p;
        this.distanceLikelyhood = distanceLikelyhood;
        this.id = id;
    }

    public Candidate(int id, TimePoint p, double distanceLikelyhood) {
        this.id = id;
        this.p = p;
        this.distanceLikelyhood = distanceLikelyhood;
    }

    public int getID() {
        return id;
    }
    public TimePoint getTimePoint() {
        return p;
    }
    public double getLikelyhood() {
        return distanceLikelyhood;
    }
    public Waypoint getWaypoint() {
        return w;
    }
}
