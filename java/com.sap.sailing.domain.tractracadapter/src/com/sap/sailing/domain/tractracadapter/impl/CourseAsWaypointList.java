package com.sap.sailing.domain.tractracadapter.impl;

import java.util.AbstractList;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.util.Util;

import difflib.Patch;

/**
 * Wraps a {@link Course} as a {@link List<Waypoint>} based on the course's {@link Course#addWaypoint(int, Waypoint)}
 * and {@link Course#removeWaypoint(Waypoint)} methods. This is convenient in case a {@link Patch<Waypoint>} is to be
 * {@link Patch#applyTo(java.util.List) applied} to a {@link Course}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class CourseAsWaypointList extends AbstractList<Waypoint> {
    private final Course course;
    
    public CourseAsWaypointList(Course course) {
        super();
        this.course = course;
    }
    
    @Override
    public void add(int index, Waypoint element) {
        course.addWaypoint(index, element);
    }

    @Override
    public Waypoint remove(int index) {
        Waypoint toRemove = get(index);
        course.removeWaypoint(toRemove);
        return toRemove;
    }

    @Override
    public Waypoint get(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int i=0;
        for (Waypoint waypoint : course.getWaypoints()) {
            if (i == index) {
                return waypoint;
            }
            i++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
        return Util.size(course.getWaypoints());
    }

}
