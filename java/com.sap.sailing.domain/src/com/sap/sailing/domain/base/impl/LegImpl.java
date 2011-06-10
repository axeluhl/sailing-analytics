package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;

public class LegImpl implements Leg {
    private final Waypoint from;
    private final Waypoint to;
    
    public LegImpl(Waypoint from, Waypoint to) {
        super();
        this.from = from;
        this.to = to;
    }

    @Override
    public Waypoint getFrom() {
        return from;
    }

    @Override
    public Waypoint getTo() {
        return to;
    }
    
    @Override
    public String toString() {
        return from + " -> " + to;
    }
}