package com.sap.sailing.domain.tracking;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;

public interface TrackedRace {
    RaceDefinition getRace();
    
    TimePoint getStart();
    
    TimePoint getFirstFinish();
    
    List<TrackedLeg> getTrackedLegs();
    
    TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg);
    
    TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg);
    
    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    Track<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
}
