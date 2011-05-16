package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;

public interface TrackedRace {
    RaceDefinition getRace();
    
    TimePoint getStart();
    
    TimePoint getFirstFinish();
    
    Iterable<TrackedLeg> getTrackedLegs();
}
