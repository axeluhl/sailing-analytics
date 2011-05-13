package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;

public interface MarkRounding extends Timed {
	Waypoint getWaypoint();
	Boat getBoat();
}
