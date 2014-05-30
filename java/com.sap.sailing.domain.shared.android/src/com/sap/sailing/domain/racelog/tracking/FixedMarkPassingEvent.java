package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

public interface FixedMarkPassingEvent extends RaceLogEvent, Revokable {
    
    public Competitor getCompetitor();
    
    public Waypoint getWaypoint();
    
    public TimePoint getTimePointOfPassing();

}
