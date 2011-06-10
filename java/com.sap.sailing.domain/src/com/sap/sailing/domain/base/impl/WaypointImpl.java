package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Waypoint;

public class WaypointImpl implements Waypoint {
    private final ControlPoint controlPoint;
    
    public boolean equals(Object o) {
        return getControlPoint() == ((Waypoint) o).getControlPoint();
    }
    
    public int hashCode() {
        return 984637 ^ getControlPoint().hashCode();
    }

    public WaypointImpl(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
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
}
