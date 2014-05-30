package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

public interface SuppressedMarkPassingsEvent extends RaceLogEvent, Revokable {
    
    public Competitor getCompetitor();
    
    public Waypoint getFirstSuppressedWaypoint();

}
