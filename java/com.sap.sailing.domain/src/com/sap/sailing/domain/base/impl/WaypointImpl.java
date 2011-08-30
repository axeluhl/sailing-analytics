package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Waypoint;

public class WaypointImpl implements Waypoint {
    private final ControlPoint controlPoint;
    private static int idCounter = 1;
    private final int id;
    
    public WaypointImpl(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
        id = idCounter++;
    }

    @Override
    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    @Override
    public String getName() {
        return getControlPoint().getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Iterable<Buoy> getBuoys() {
        return getControlPoint().getBuoys();
    }

    /**
     * Note that a waypoint is not compared by its identity but by the control point it represents.
     */
    @Override
    public Integer getId() {
        return id;
    }
    
}
