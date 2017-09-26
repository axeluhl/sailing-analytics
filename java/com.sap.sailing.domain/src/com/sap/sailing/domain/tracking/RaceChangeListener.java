package com.sap.sailing.domain.tracking;

import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sse.common.TimePoint;


public interface RaceChangeListener extends CourseListener {
    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor);
    
    void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack);
    
    void firstGPSFixReceived();
    
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

    void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking);
    
    void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking);
    
    void startTimeReceivedChanged(TimePoint startTimeReceived);
    
    /**
     * Fired when the {@link TrackedRace#getStartOfRace() start of race} time point has changed for the tracked race
     * observed by this listener. There can be several reasons for this to happen, among them the setting of the start
     * time by the race committee app, or receiving a new start time from the tracking provider, or receiving a new
     * start mark passing with start time inference enabled, or a switch of the flag determining whether start mark
     * passing-based start time inference is active or not.
     */
    void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace);
    
    /**
     * Fired when in any of the attached {@link RaceLog}s a {@link RaceLogRaceStatusEvent} or a {@link RaceLogPassChangeEvent}
     * or a {@link RaceLogRevokeEvent} has caused a change in the {@link RaceState#getFinishedTime() finished time} inferred
     * from that race log.
     */
    void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime);

    void delayToLiveChanged(long delayToLiveInMillis);

    void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude);

    void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus);
    
    void competitorSensorTrackAdded(DynamicSensorFixTrack<Competitor, ?> track);
    
    void competitorSensorFixAdded(Competitor competitor, String trackName, SensorFix fix);
    
    void regattaLogAttached(RegattaLog regattaLog);
    
    void raceLogAttached(RaceLog raceLog);
    
    void raceLogDetached(RaceLog raceLog);
}
