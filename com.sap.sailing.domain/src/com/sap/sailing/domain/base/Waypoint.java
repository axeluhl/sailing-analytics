package com.sap.sailing.domain.base;

/**
 * Waypoints constitute {@link Course}s and demarcate their {@link Leg}s. A waypoint's
 * position is defined by a {@link ControlPoint}, such as a {@link Buoy} or a
 * {@link Gate}. The same control point can be used by multiple waypoints.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Waypoint extends Named {
    ControlPoint getControlPoint();
    Iterable<Buoy> getBuoys();
}
