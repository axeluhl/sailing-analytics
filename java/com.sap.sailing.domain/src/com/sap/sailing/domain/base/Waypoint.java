package com.sap.sailing.domain.base;

/**
 * Waypoints constitute {@link Course}s and demarcate their {@link Leg}s. A waypoint's position is defined by a
 * {@link ControlPoint}, such as a {@link Buoy} or a {@link Gate}. The same control point can be used by multiple
 * waypoints.<p>
 * 
 * Two waypoints {@link Object#equals(Object) equal} each other if they have the <em>same</em>
 * {@link #getControlPoint() control point}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Waypoint extends Named, WithID {
    ControlPoint getControlPoint();

    Iterable<Buoy> getBuoys();
}
