package com.sap.sailing.domain.base;

import java.io.Serializable;

/**
 * When two legs are compared, their natural order is by the index of their {@link #getFrom() start waypoint} in the
 * {@link Course} to which they belong. A leg closer to the beginning of the course ranks "less" than one that is closer
 * to the end of the course.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Leg extends Serializable, Comparable<Leg> {
    Waypoint getFrom();

    Waypoint getTo();
    
    /**
     * The index of this leg's {@link #getFrom() start waypoint} in the sequence of the
     * {@link Course}'s {@link Course#getWaypoints waypoints}. The first waypoint in
     * the course has index 0.
     */
    int getZeroBasedIndexOfStartWaypoint();
}
