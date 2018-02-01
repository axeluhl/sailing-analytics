package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NauticalSide;

public interface MarkPassingManeuver extends Maneuver {
    Waypoint getWaypointPassed();
    
    NauticalSide getSide();
}
