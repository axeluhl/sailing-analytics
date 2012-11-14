package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

/**
 * Waypoints constitute {@link Course}s and demarcate their {@link Leg}s. A waypoint's position is defined by a
 * {@link ControlPoint}, such as a {@link SingleMark} or a {@link Gate}. The same control point can be used by multiple
 * waypoints.<p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Waypoint extends Named, WithID, IsManagedByDomainFactory {
    ControlPoint getControlPoint();

    Iterable<SingleMark> getMarks();
}
