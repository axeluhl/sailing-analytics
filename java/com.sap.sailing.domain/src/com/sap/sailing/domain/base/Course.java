package com.sap.sailing.domain.base;

import java.util.List;

/**
 * 
 * A course consists of a sequence of {@link Waypoint}s. The {@link Leg}s extend between the adjacent waypoints.
 * Therefore, there is one waypoint more than there are legs.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Course extends Named {
    List<Leg> getLegs();

    Iterable<Waypoint> getWaypoints();
    
    /**
     * Starts searching at position <code>start</code> in {@link #getWaypoints()} for a waypoint
     * whose {@link Waypoint#getControlPoint() control point} is identical to <code>controlPoint</code>.
     * If no such waypoint is found, <code>null</code> is returned.
     * 
     * @param start 0-based
     */
    Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start);

    /**
     * Position of the waypoint in {@link #getWaypoints()}, 0-based, or -1
     * if waypoint doesn't exist in {@link #getWaypoints()}.
     */
    int getIndexOfWaypoint(Waypoint waypoint);

    Waypoint getFirstWaypoint();
    
    Waypoint getLastWaypoint();
    
    void addCourseListener(CourseListener listener);

    void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd);

    void removeWaypoint(Waypoint waypointToRemove);
}
