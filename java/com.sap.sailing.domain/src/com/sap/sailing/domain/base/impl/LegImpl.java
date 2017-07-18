package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;

public class LegImpl implements Leg {
    private static final long serialVersionUID = -6730954156357241076L;
    private final CourseImpl course;
    private final int indexOfStartWaypoint;
    
    public LegImpl(CourseImpl course, int indexOfStartWaypoint) {
        super();
        this.course = course;
        this.indexOfStartWaypoint = indexOfStartWaypoint;
    }

    @Override
    public Waypoint getFrom() {
        return course.getWaypoint(indexOfStartWaypoint);
    }

    @Override
    public Waypoint getTo() {
        return course.getWaypoint(indexOfStartWaypoint+1);
    }
    
    @Override
    public String toString() {
        return getFrom() + " -> " + getTo();
    }

    @Override
    public int getZeroBasedIndexOfStartWaypoint() {
        return indexOfStartWaypoint;
    }

    @Override
    public int compareTo(Leg o) {
        return getZeroBasedIndexOfStartWaypoint() - o.getZeroBasedIndexOfStartWaypoint();
    }
}