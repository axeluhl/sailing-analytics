package com.sap.sailing.domain.base;

import java.util.List;

import com.sap.sse.common.Named;

/**
 * Base interface for courses consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface CourseBase extends Named {
    /**
     * Clients can safely iterate over the resulting list because it's a copy which therefore won't reflect
     * waypoint additions and removals. 
     */
    List<Leg> getLegs();

    /**
     * @return a non-live copy of the waypoints of this course; the creation of the copy is thread safe
     */
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

    /**
     * @return the course's last waypoint if not empty, <code>null</code> otherwise. Acquires the course's read lock.
     */
    Waypoint getLastWaypoint();

    void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd);

    void removeWaypoint(int zeroBasedPosition);

    Leg getFirstLeg();
}
