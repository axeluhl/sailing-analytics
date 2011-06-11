package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;

public class CourseImpl extends NamedImpl implements Course {
    private final List<Waypoint> waypoints;
    private final Map<Waypoint, Integer> waypointIndexes;
    private final List<Leg> legs;
    
    public CourseImpl(String name, Iterable<Waypoint> waypoints) {
        super(name);
        this.waypoints = new ArrayList<Waypoint>();
        waypointIndexes = new HashMap<Waypoint, Integer>();
        legs = new ArrayList<Leg>();
        Iterator<Waypoint> waypointIter = waypoints.iterator();
        int i=0;
        if (waypointIter.hasNext()) {
            Waypoint previous = waypointIter.next();
            this.waypoints.add(previous);
            waypointIndexes.put(previous, i++);
            while (waypointIter.hasNext()) {
                Waypoint current = waypointIter.next();
                this.waypoints.add(current);
                waypointIndexes.put(current, i++);
                Leg leg = new LegImpl(this, i);
                legs.add(leg);
                previous = current;
            }
        }
    }
    
    /**
     * For access by {@link LegImpl}
     */
    Waypoint getWaypoint(int i) {
        return waypoints.get(i);
    }

    @Override
    public List<Leg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    @Override
    public Iterable<Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getName());
        result.append(": ");
        boolean first = true;
        for (Waypoint waypoint : getWaypoints()) {
            if (!first) {
                result.append(" -> ");
            } else {
                first = false;
            }
            result.append(waypoint);
        }
        return result.toString();
    }

    @Override
    public int getIndexOfWaypoint(Waypoint waypoint) {
        int result = -1;
        Integer indexEntry = waypointIndexes.get(waypoint);
        if (indexEntry != null) {
            result = indexEntry;
        }
        return result;
    }

    @Override
    public Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
        if (start > legs.size()) {
            throw new IllegalArgumentException("Starting to search beyond end of course: "+start+" vs. "+(legs.size()+1));
        }
        int i=0;
        for (Waypoint waypoint : getWaypoints()) {
            if (i >= start && waypoint.getControlPoint() == controlPoint) {
                return waypoint;
            }
            i++;
        }
        return null;
    }

    @Override
    public Waypoint getFirstWaypoint() {
        return waypoints.get(0);
    }

    @Override
    public Waypoint getLastWaypoint() {
        return waypoints.get(waypoints.size()-1);
    }
    
}
