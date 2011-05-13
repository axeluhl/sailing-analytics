package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;

public class CourseImpl extends NamedImpl implements Course {
    private final Iterable<Waypoint> waypoints;
    private final List<Leg> legs;
    
    public CourseImpl(String name, Iterable<Waypoint> waypoints) {
        super(name);
        this.waypoints = waypoints;
        legs = new ArrayList<Leg>();
        Iterator<Waypoint> waypointIter = waypoints.iterator();
        if (waypointIter.hasNext()) {
            Waypoint previous = waypointIter.next();
            while (waypointIter.hasNext()) {
                Waypoint current = waypointIter.next();
                Leg leg = new LegImpl(previous, current);
                legs.add(leg);
                previous = current;
            }
        }
    }

    @Override
    public Iterable<Leg> getLegs() {
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
    
}
