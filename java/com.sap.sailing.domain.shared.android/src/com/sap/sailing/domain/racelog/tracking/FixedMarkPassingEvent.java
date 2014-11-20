package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;
import com.sap.sse.common.TimePoint;

public interface FixedMarkPassingEvent extends RaceLogEvent, Revokable {
    
    public TimePoint getTimePointOfFixedPassing();

    Integer getZeroBasedIndexOfPassedWaypoint();

}
