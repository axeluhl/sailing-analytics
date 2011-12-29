package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.common.WindSource;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);
    
    void recordWind(Wind wind, WindSource windSource);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    /**
     * Yields the track describing <code>buoy</code>'s movement over time; never <code>null</code> because a
     * new track will be created in case no track was present for <code>buoy</code> so far.
     */
    DynamicGPSFixTrack<Buoy, GPSFix> getOrCreateTrack(Buoy buoy);

    // TODO need another listener protocol for general changes in ranking and leg completion

    /**
     * Updates all mark passings for <code>competitor</code> for this race. The
     * mark passings must be provided in the order of the race's course and in
     * increasing time stamps. Calling this method replaces all previous mark passings
     * for this race for <code>competitor</code> and ensures that the "leaderboard"
     * and all other derived information are updated accordingly. 
     */
    void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings);

    /**
     * Sets the start time as received from the tracking infrastructure. This isn't necessarily
     * what {@link #getStart()} will deliver which assumes that the time announced here may be
     * significantly off.
     */
    void setStartTimeReceived(TimePoint start);

    void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed);

    void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind);
    
    DynamicTrackedEvent getTrackedEvent();

    void setWindSource(WindSource windSource);

    /**
     * If and only if <code>raceIsKnownToStartUpwind</code> is <code>true</code>, this tracked race is allowed to use
     * the start leg's direction as a fallback for estimating the wind direction.
     */
    void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind);
}
