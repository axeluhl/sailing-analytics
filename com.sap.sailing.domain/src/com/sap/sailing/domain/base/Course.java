package com.sap.sailing.domain.base;

/**
 * 
 * A course consists of a sequence of {@link Waypoint}s. The {@link Leg}s extend between the adjacent waypoints.
 * Therefore, there is one waypoint more than there are legs.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Course extends Named {
    Iterable<Leg> getLegs();

    Iterable<Waypoint> getWaypoints();

    /**
     * Position of the waypoint in {@link #getWaypoints()}, 0-based, or -1
     * if waypoint doesn't exist in {@link #getWaypoints()}.
     */
    int getIndexOfWaypoint(Waypoint waypoint);
}
