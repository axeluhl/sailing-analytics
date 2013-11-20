package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;

public class Candidate {
    private Waypoint w;
    private TimePoint p;
    private double cost;
    private int id;

    public Candidate(Waypoint w, TimePoint p, double cost, int id) {
        this.w = w;
        this.p = p;
        this.cost = cost;
        this.id = id;
    }

    public Candidate(int id, TimePoint p, double cost) {
        this.id = id;
        this.p = p;
        this.cost = cost;
    }

    public int getID() {
        return id;
    }
    public TimePoint getTimePoint() {
        return p;
    }
    public double getCost() {
        return cost;
    }
    public Waypoint getWaypoint() {
        return w;
    }
}
