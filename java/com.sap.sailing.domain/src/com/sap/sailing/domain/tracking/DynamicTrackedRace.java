package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.TimePoint;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);
    
    void recordFix(Mark mark, GPSFix fix);
    
    /**
     * @return True if the specified wind has been accepted and added to this race's wind track and database, else false.
     */
    boolean recordWind(Wind wind, WindSource windSource);

    void removeWind(Wind wind, WindSource windSource);

    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    /**
     * Yields the track describing <code>mark</code>'s movement over time; never <code>null</code> because a
     * new track will be created in case no track was present for <code>mark</code> so far.
     */
    DynamicGPSFixTrack<Mark, GPSFix> getOrCreateTrack(Mark mark);

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
    
    /** Sets the start of tracking as received from the tracking infrastructure.
     * This isn't necessarily what {@link #getStartOfTracking()} will deliver because we might consider other values to
     * calculate the start of tracking.
     */
    void setStartOfTrackingReceived(TimePoint startOfTrackingReceived);

    /** Sets the end of tracking as received from the tracking infrastructure.
     * This isn't necessarily what {@link #getEndOfTracking()} will deliver because we might consider other values to
     * calculate the end of tracking.
     */
    void setEndOfTrackingReceived(TimePoint endOfTrackingReceived);

    void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed);

    void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind);
    
    /**
     * Same as {@link #setDelayToLiveInMillis(long)}, except that afterwards, a {@link #setDelayToLiveInMillis(long)} will no longer
     * take effect.
     */
    void setAndFixDelayToLiveInMillis(long delayToLiveInMillis);
    
    /**
     * Updates the value returned by {@link #getDelayToLiveInMillis()}, except that {@link #setAndFixDelayToLiveInMillis(long)} was called
     * on this object before, in which case this call takes no effect.
     */
    void setDelayToLiveInMillis(long delayToLiveInMillis);
    
    DynamicTrackedRegatta getTrackedRegatta();

    /**
     * If and only if <code>raceIsKnownToStartUpwind</code> is <code>true</code>, this tracked race is allowed to use
     * the start leg's direction as a fallback for estimating the wind direction.
     */
    void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind);
    
    void setStatus(TrackedRaceStatus newStatus);

    /**
     * whenever a new course design is published by the race committee and the appropriate event occurs in the race log,
     * this method is called to propagate the course design to the tracking provider.
     * 
     * @param courseDesign
     *            the new course design to be published
     */
    void onCourseDesignChangedByRaceCommittee(CourseBase courseDesign);
    
    void onStartTimeChangedByRaceCommittee(TimePoint newStartTime);
    
    void onAbortedByRaceCommittee(Flags flag);

    void invalidateStartTime();
    
    void invalidateEndTime();
}
