package com.sap.sailing.util;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sse.common.Util;

import difflib.Patch;

/**
 * Wraps a {@link Course} as a {@link List<Waypoint>} based on the course's {@link Course#addWaypoint(int, Waypoint)}
 * and {@link Course#removeWaypoint(int)} methods. This is convenient in case a {@link Patch<Waypoint>} is to be
 * {@link Patch#applyTo(java.util.List) applied} to a {@link Course}.
 * <p>
 * 
 * Executions of all {@link #add(int, Waypoint)} operations that would lead to duplicate waypoint occurrences are
 * replaced by an insertion of a proxy waypoint. The insertion of the waypoint passed to {@link #add(int, Waypoint)} is
 * delayed until its occurrence is removed. This is necessary because moving of waypoints may be represented as a
 * sequence of insert/remove (instead of remove/insert) which would lead to duplicate {@link Waypoint} occurrences in
 * the course which is not permitted.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class CourseAsWaypointList extends AbstractList<Waypoint> {
    private final Course course;
    
    /**
     * Keys are the waypoints to be inserted, values are the proxy waypoints used as placeholder instead of the
     * key waypoints. The placeholder waypoint will be replaced by the key waypoint when {@link #remove(int)} removes
     * the key waypoint.
     */
    private final Map<Waypoint, Waypoint> delayedInserts;
    
    private static final ControlPoint dummyControlPointForPlaceholderWaypoints = new MarkImpl("Dummy mark for placeholder waypoints");
    
    public CourseAsWaypointList(Course course) {
        super();
        this.course = course;
        this.delayedInserts = new HashMap<Waypoint, Waypoint>();
    }
    
    @Override
    public void add(int index, Waypoint element) {
        if (course.getIndexOfWaypoint(element) == -1) {
            course.addWaypoint(index, element); // no duplicate would be produced, immediate insert is safe
        } else {
            // a duplicate would result from inserting the waypoint; insert a placeholder waypoint and remember it
            Waypoint placeholder = new WaypointImpl(dummyControlPointForPlaceholderWaypoints);
            delayedInserts.put(element, placeholder);
            course.addWaypoint(index, placeholder);
        }
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Waypoint)) {
            return -1;
        } else {
            return course.getIndexOfWaypoint((Waypoint) o);
        }
    }

    @Override
    public Waypoint remove(int index) {
        Waypoint toRemove = get(index);
        course.removeWaypoint(index);
        Waypoint placeholder = delayedInserts.remove(toRemove);
        if (placeholder != null) {
            // now it's safe to replace the placeholder by the real waypoint that was passed to add(...)
            int placeholderIndex = course.getIndexOfWaypoint(placeholder);
            course.removeWaypoint(placeholderIndex);
            course.addWaypoint(placeholderIndex, toRemove);
        }
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
