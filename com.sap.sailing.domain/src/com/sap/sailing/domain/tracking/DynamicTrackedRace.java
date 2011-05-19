package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    void addListener(RawListener<Competitor, GPSFixMoving> listener);

    @Override
    DynamicTrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg);

    @Override
    DynamicTrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg);
}
