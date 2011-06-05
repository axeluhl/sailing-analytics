package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);
    
    void recordWind(Wind wind, WindSource windSource);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    void addListener(RaceChangeListener<Competitor> listener);
    
    // TODO need another listener protocol for general changes in ranking and leg completion

    /**
     * Updates all mark passings for <code>competitor</code> for this race. The
     * mark passings must be provided in the order of the race's course and in
     * increasing time stamps. Calling this method replaces all previous mark passings
     * for this race for <code>competitor</code> and ensures that the "leaderboard"
     * and all other derived information are updated accordingly. 
     */
    void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings);

    void setStart(TimePoint start);

    void setFirstFinish(TimePoint stop);
}
