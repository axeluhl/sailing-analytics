package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sse.common.TimePoint;

public interface FixedMarkPassingEvent extends RaceLogEvent, Revokable {
    
    public TimePoint getTimePointOfFixedPassing();

    Integer getZeroBasedIndexOfPassedWaypoint();

}
