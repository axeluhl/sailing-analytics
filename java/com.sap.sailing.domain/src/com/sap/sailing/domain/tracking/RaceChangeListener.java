package com.sap.sailing.domain.tracking;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sse.common.TimePoint;


public interface RaceChangeListener extends CourseListener {
    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor);
    
    void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack);
    
    /**
     * Invoked after the mark passings have been updated in the {@link TrackedRace}.
     * 
     * @param oldMarkPassings
     *            the mark passings replaced by <code>markPassings</code>, keyed by the waypoints
     */
    void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

    void windDataReceived(Wind wind, WindSource windSource);
    
    void windDataRemoved(Wind wind, WindSource windSource);

    void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

    void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived);
    
    /**
     * Fired when the {@link TrackedRace#getStartOfRace() start of race} time point has changed for the tracked race
     * observed by this listener. There can be several reasons for this to happen, among them the setting of the start
     * time by the race committee app, or receiving a new start time from the tracking provider, or receiving a new
     * start mark passing with start time inference enabled, or a switch of the flag determining whether start mark
     * passing-based start time inference is active or not.
     */
    void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace);

    void delayToLiveChanged(long delayToLiveInMillis);

    void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude);

    void statusChanged(TrackedRaceStatus newStatus);
}
