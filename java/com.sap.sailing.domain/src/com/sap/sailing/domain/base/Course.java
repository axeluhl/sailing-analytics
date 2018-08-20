package com.sap.sailing.domain.base;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Renamable;
import com.sap.sse.common.Util;

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
public interface Course extends CourseBase, Renamable {
    void lockForRead();

    void unlockAfterRead();

    void addCourseListener(CourseListener listener);

    void removeCourseListener(CourseListener listener);

    /**
     * Carefully merges the new list of control points into this course by constructing a minimal difference between the
     * control point list and the control points referenced by this course's waypoints. Change events are propagated
     * to the registered {@link CourseListener}s as if {@link #addWaypoint(int, Waypoint)} and {@link #removeWaypoint(int)}
     * had been used.
     */
    void update(Iterable<Util.Pair<ControlPoint, PassingInstruction>> newControlPoints, DomainFactory baseDomainFactory) throws PatchFailedException;
    
    int getNumberOfWaypoints();

    Leg getLeg(int zeroBasedIndexOfWaypoint);
}
