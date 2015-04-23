package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.Timed;

public interface MarkPassing extends Timed {
    Waypoint getWaypoint();

    Competitor getCompetitor();
}
