package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.impl.NamedImpl;

public class CourseDataImpl extends NamedImpl implements CourseBase {

    private static final long serialVersionUID = 1389216430392253253L;
    
    private final List<Waypoint> waypoints;

    public CourseDataImpl(String name) {
        super(name);
        this.waypoints = new ArrayList<Waypoint>();
    }

    @Override
    public List<Leg> getLegs() {
        return null;
    }

    @Override
    public Iterable<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    @Override
    public Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
        Waypoint result = null;
        List<Waypoint> subList = this.waypoints.subList(start, this.waypoints.size() - 1);
        for (Waypoint waypoint : subList) {
           if (waypoint.getControlPoint().equals(controlPoint)) {
               result = waypoint;
               break;
           }
        }
        return result;
    }

    @Override
    public int getIndexOfWaypoint(Waypoint waypoint) {
        return this.waypoints.indexOf(waypoint);
    }
    
    @Override
    public Waypoint getFirstWaypoint() {
        return this.waypoints.get(0);
    }

    @Override
    public Waypoint getLastWaypoint() {
        return this.waypoints.get(this.waypoints.size() - 1);
    }

    @Override
    public void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd) {
        this.waypoints.add(zeroBasedPosition, waypointToAdd);
    }

    @Override
    public void removeWaypoint(int zeroBasedPosition) {
        this.waypoints.remove(zeroBasedPosition);
    }

    @Override
    public Leg getFirstLeg() {
        return null;
    }

}
