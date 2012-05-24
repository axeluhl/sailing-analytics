package com.sap.sailing.domain.tracking;

import java.util.Map;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;


public interface RaceChangeListener {
    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor);
    
    void buoyPositionChanged(GPSFix fix, Buoy buoy);
    
    /**
     * Invoked after the mark passings have been updated in the {@link TrackedRace}.
     * 
     * @param oldMarkPassings
     *            the mark passings replaced by <code>markPassings</code>, keyed by the waypoints
     */
    void markPassingReceived(Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

    void windDataReceived(Wind wind, WindSource windSource);
    
    void windDataRemoved(Wind wind, WindSource windSource);

    void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

    void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived);

    void delayToLiveChanged(long delayToLiveInMillis);

    void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude);
}
