package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;

public interface MarkPassing extends Timed {
    Waypoint getWaypoint();

    Competitor getCompetitor();
}
