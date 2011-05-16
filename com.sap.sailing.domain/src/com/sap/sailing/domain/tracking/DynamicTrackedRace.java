package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    void addListener(RawListener<GPSFixMoving> listener);
}
