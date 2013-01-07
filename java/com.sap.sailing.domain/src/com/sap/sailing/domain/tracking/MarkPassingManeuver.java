package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Tack;

public interface MarkPassingManeuver extends Maneuver {
    Waypoint getWaypointPassed();
    
    Tack getSide();
}
