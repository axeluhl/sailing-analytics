package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.WithID;
import com.sap.sse.common.Named;

/**
 * Waypoints constitute {@link Course}s and demarcate their {@link Leg}s. A waypoint's position is defined by a
 * {@link ControlPoint}, such as a {@link Mark} or a {@link ControlPointWithTwoMarks}. The same control point can be used by multiple
 * waypoints.<p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Waypoint extends Named, WithID, IsManagedByCache {
    ControlPoint getControlPoint();

    Iterable<Mark> getMarks();
    
    /**
     * Return the passing side for the waypoint. Null is allowed e.g. when the waypoint is a gate.
     * @return
     */
    PassingInstruction getPassingInstructions();

    /**
     * A waypoint may be defined by using a single mark and a bearing from that mark, leading to a
     * virtual line that the object needs to cross. For other waypoints, this methods returns <code>null</code>.
     */
    Bearing getFixedBearing();
}
