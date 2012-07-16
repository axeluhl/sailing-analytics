package com.sap.sailing.domain.base;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.domain.common.Named;

import difflib.PatchFailedException;

/**
 * A course consists of a sequence of {@link Waypoint}s. The {@link Leg}s extend between the adjacent waypoints.
 * Therefore, there is one waypoint more than there are legs.
 * <p>
 * 
 * A course maintains a re-entrant read/write lock (see {@link ReentrantReadWriteLock}. Individual reading methods
 * internally use the read lock to protect against concurrent modifications through {@link #removeWaypoint(int)} or
 * {@link #addWaypoint(int, Waypoint)} which each obtain the write lock. However, if callers expect the waypoint or leg
 * obtained from a reading call to remain valid, holding the read lock is required. Otherwise, particularly a
 * {@link #removeWaypoint(int)} call may turn a leg and of course the waypoint invalid and therefore violate the
 * reader's assumptions. The write lock is still held while propagating the updates to all {@link CourseListener}s
 * registered. It is of course necessary to always use {@link #lockForRead()} and {@link #unlockAfterRead()}
 * symmetrically, where the {@link #unlockAfterRead()} call should happen in a <code>finally</code> clause to guarantee
 * execution.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Course extends Named {
    void lockForRead();
    
    void unlockAfterRead();
    
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
    
    Waypoint getLastWaypoint();
    
    void addCourseListener(CourseListener listener);
    
    void removeCourseListener(CourseListener listener);

    void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd);

    void removeWaypoint(int zeroBasedPosition);

    /**
     * Carefully merges the new list of control points into this course by constructing a minimal difference between the
     * control point list and the control points referenced by this course's waypoints. Change events are propagated
     * to the registered {@link CourseListener}s as if {@link #addWaypoint(int, Waypoint)} and {@link #removeWaypoint(int)}
     * had been used.
     */
    void update(List<ControlPoint> newControlPoints, DomainFactory baseDomainFactory) throws PatchFailedException;

    Iterable<Leg> getLegsAdjacentTo(Buoy buoy);

}
