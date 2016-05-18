package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;

public interface DynamicTrackedRace extends TrackedRace {
    /**
     * Records a position and speed and course over ground fix for a competitor, but only if the fix's {@link GPSFixMoving#getTimePoint()}
     * is within this race's {@link #getStartOfTracking() start} and {@link #getEndOfTracking() end} of tracking time interval.
     */
    default void recordFix(Competitor competitor, GPSFixMoving fix) {
        recordFix(competitor, fix, /* onlyWhenInTrackingTimeInterval */ true);
    }

    /**
     * Records a position and speed and course over ground fix for a competitor. If
     * {@code onlyWhenInTrackingTimeInterval} is {@code true}, the fix is recorded only if the fix's
     * {@link GPSFixMoving#getTimePoint()} is within this race's {@link #getStartOfTracking() start} and
     * {@link #getEndOfTracking() end} of tracking time interval. If {@code onlyWhenInTrackingTimeInterval} is
     * {@code false}, the fix is recorded regardless of this race's tracking times interval.
     */
    void recordFix(Competitor competitor, GPSFixMoving fix, boolean onlyWhenInTrackingTimeInterval);
    
    /**
     * Records a position fix for a mark, but only if the fix's {@link GPSFixMoving#getTimePoint()} is within this
     * race's {@link #getStartOfTracking() start} and {@link #getEndOfTracking() end} of tracking time interval.
     */
    default void recordFix(Mark mark, GPSFix fix) {
        recordFix(mark, fix, /* onlyWhenInTrackingTimeInterval */ true);
    }
    
    /**
     * Records a position fix for a mark. If {@code onlyWhenInTrackingTimeInterval} is {@code true}, the fix is recorded
     * only if the fix's {@link GPSFixMoving#getTimePoint()} is within this race's {@link #getStartOfTracking() start}
     * and {@link #getEndOfTracking() end} of tracking time interval. If {@code onlyWhenInTrackingTimeInterval} is
     * {@code false}, the fix is recorded regardless of this race's tracking times interval.
     */
    void recordFix(Mark mark, GPSFix fix, boolean onlyWhenInTrackingTimeInterval);

    /**
     * Inserts a <code>wind</code> fix into a {@link WindTrack} for the <code>windSource</code> if the current filtering
     * rules accept the wind fix. Filtering applies based upon timing considerations, assuming that wind fixes are not
     * relevant if they are outside of the tracking interval. There may be exceptions for races acting as default wind
     * acceptors in case no other race in the regatta would accept the wind fix.
     * 
     * @return True if the specified wind has been accepted and added to this race's wind track and database, else false.
     */
    default boolean recordWind(Wind wind, WindSource windSource) {
        return recordWind(wind, windSource, /* applyFilter */ true);
    }
    
    /**
     * Like {@link #recordWind(Wind, WindSource)}, only that filtering may be disabled by setting
     * <code>applyFilter</code> to <code>false</code>.
     */
    boolean recordWind(Wind wind, WindSource windSource, boolean applyFilter);

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
     * Updates all mark passings for <code>competitor</code> for this race. The mark passings must be provided in the
     * order of the race's course and in increasing time stamps. Calling this method replaces all previous mark passings
     * for this race for <code>competitor</code> and ensures that the "leaderboard" and all other derived information
     * are updated accordingly.
     * <p>
     * 
     * When an attached {@link RaceLog} has a {@link RaceLogFinishPositioningConfirmedEvent} that sets a
     * {@link CompetitorResult#getFinishingTime() finishing time} for a competitor, it will be used to override the
     * {@link MarkPassing#getTimePoint() time point} of the finishing waypoint's mark passing or, if no mark passing for
     * the finishing waypoint exists yet for that competitor, create one. This can, in particular, be helpful when
     * determining the time sailed for the {@code competitor} in order to determine the calculated time after applying
     * any handicap rules and metrics.<p>
     */
    void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings);
    
    /**
     * When there is a significant change in the race logs attached to this race, such as adding another race log or
     * removing a race log or switching to another pass in one of the race logs attached, the effects on the
     * valid {@link RaceLogFinishPositioningConfirmedEvent} are analyzed, and if relevant, the mark passings
     * for the finish line that are affected will be {@link #updateMarkPassings(Competitor, Iterable) updated}.
     */
    void updateMarkPassingsAfterRaceLogChanges();

    /**
     * Sets the start time as received from the tracking infrastructure. This isn't necessarily
     * what {@link #getStart()} will deliver which assumes that the time announced here may be
     * significantly off.
     */
    void setStartTimeReceived(TimePoint start);

    /**
     * A new finished time has been received by the {@link DynamicTrackedRaceLogListener} and is announced to this race
     * by calling this method. The {@link RaceChangeListener}s will be
     * {@link RaceChangeListener#finishedTimeChanged(TimePoint, TimePoint) notified} about this change, and the result
     * of {@link #getFinishedTime()} will return the {@code newFinishedTime} after this call returns.
     */
    void setFinishedTime(final TimePoint newFinishedTime);
    
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
