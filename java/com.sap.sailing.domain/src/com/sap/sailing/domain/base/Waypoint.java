package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.WithID;

/**
 * Waypoints constitute {@link Course}s and demarcate their {@link Leg}s. A waypoint's position is defined by a
 * {@link ControlPoint}, such as a {@link Mark} or a {@link Gate}. The same control point can be used by multiple
 * waypoints.<p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Waypoint extends Named, WithID, IsManagedByDomainFactory {
    ControlPoint getControlPoint();

    Iterable<Mark> getMarks();
    
    /**
     * Return the passing side for the waypoint. Null is allowed e.g. when the waypoint is a gate.
     * @return
     */
    NauticalSide getPassingSide();
}
