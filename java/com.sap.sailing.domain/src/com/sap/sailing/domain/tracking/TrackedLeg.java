package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TargetTimeInfo.LegTargetTimeInfo;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public interface TrackedLeg extends Serializable {
    Leg getLeg();
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    TrackedRace getTrackedRace();

    /**
     * Determines whether the current {@link #getLeg() leg} is +/- {@link LegType#UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the current wind's bearing.
     */
    boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException;
    
    LegType getLegType(TimePoint at) throws NoWindException;

    /**
     * Computes the ranks of all competitors in this leg in one sweep. There are two advantages of this operation over
     * the more fine-grained {@link TrackedLegOfCompetitor#getRank(TimePoint)}:
     * <ol>
     * <li>all ranks are available in one call</li>
     * <li>no incoming events can influence ranks between separate calls to
     * {@link TrackedLegOfCompetitor#getRank(TimePoint)}</li>
     * </ol>
     * 
     * The iteration order of the linked hash map returned is leader to last.
     */
    LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint);

    /**
     * Same as {@link #getRanks(TimePoint)}, only that a cache for wind and leg type
     * and bearing data can be passed to avoid redundant calculations during a single
     * round trip.
     */
    LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    Bearing getLegBearing(TimePoint at);

    /**
     * Same as {@link #getLegBearing(TimePoint)}, but giving the caller the possibility to provide a cache for
     * mark positions and related position and bearing data
     * 
     * @param markPositionCache
     *            a cache for this tracked leg's {@link MarkPositionAtTimePointCache#getTrackedRace() race} and the
     *            {@link MarkPositionAtTimePointCache#getTimePoint() timePoint} passed
     */
    Bearing getLegBearing(TimePoint at, MarkPositionAtTimePointCache markPositionCache);

    /**
     * Returns the positive (absolute) distance of <code>p</code> to this leg's course middle line at <code>timePoint</code>,
     * based on the position of the waypoints delimiting this leg at that time.
     */
    Distance getAbsoluteCrossTrackError(Position p, TimePoint timePoint);

    /**
     * Returns the (signed) distance of <code>p</code> to this leg's course middle line at <code>timePoint</code>,
     * based on the position of the waypoints delimiting this leg at that time. Negative distances mean "left" of the
     * course middle line looking in the direction of the leg, positive distances mean "right."
     */
    Distance getSignedCrossTrackError(Position p, TimePoint timePoint);

    /**
     * Must be called when the start and finish waypoint of this leg may have changed.
     */
    void waypointsMayHaveChanges();
    
    /**
     * Computes the great-circle distance of this leg based on the position of the adjacent marks at <code>timePoint</code>.
     * If any of the positions of the two adjacent marks cannot be found, <code>null</code> is returned.
     */
    Distance getGreatCircleDistance(TimePoint timePoint);

    /**
     * Same as {@link #getGreatCircleDistance(TimePoint)}, but giving the caller the possibility to provide a cache for
     * mark positions and related position and bearing data
     * 
     * @param markPositionCache
     *            a cache for this tracked leg's {@link MarkPositionAtTimePointCache#getTrackedRace() race} and the
     *            {@link MarkPositionAtTimePointCache#getTimePoint() timePoint} passed
     */
    Distance getGreatCircleDistance(TimePoint timePoint, MarkPositionAtTimePointCache markPositionCache);

    /**
     * If the current {@link #getLeg() leg} is +/- {@link LegType#UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the wind's bearing, <code>pos1</code> is projected onto the line crossing <code>pos2</code> in the
     * wind's bearing, and the distance from the projection to <code>pos2</code> is returned. Otherwise, it is assumed
     * that the leg is neither an upwind nor a downwind leg, and hence the along-track distance to <code>mark</code> is
     * returned. The distance returned from this method is always positive. See also {@link #getWindwardDistance}.
     * @param at
     *            the wind estimation is performed for this point in time
     */
    Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode);

    /**
     * Same as {@link #getAbsoluteWindwardDistance(Position, Position, TimePoint, WindPositionMode)}, only that a cache
     * for leg types, wind data and leg bearings can be passed to save the effort of redundant calculations of these
     * values.
     */
    Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache);

    /**
     * Same as {@link #getWindwardDistance(Position, Position, TimePoint, WindPositionMode)}, only that a cache
     * for leg types, wind data and leg bearings can be passed to save the effort of redundant calculations of these
     * values.
     */
    Distance getWindwardDistance(final Position pos1, final Position pos2, TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache);

    /**
     * Same as {@link #getAbsoluteWindwardDistance(Position, Position, TimePoint, WindPositionMode)}, but this method considers the leg's
     * direction and will return a negative distance if <code>pos1</code> is already "ahead" of <code>pos2</code> in the
     * leg's direction, or a positive distance otherwise.
     */
    Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode);
    
    /**
     * Computes the windward distance for upwind/downwind legs or the great circle distance between this leg's waypoints
     * that competitors need to travel in this leg. Due to the dynamics of the wind direction and the mark positions,
     * the result is time dependent. The wind direction is determined at the leg's middle at the time point <code>at</code>.
     */
    Distance getWindwardDistance(TimePoint at, WindLegTypeAndLegBearingCache cache);

    Distance getAbsoluteWindwardDistance(TimePoint at, WindLegTypeAndLegBearingCache cache);

    /**
     * Computes the windward distance for upwind/downwind legs or the great circle distance between this leg's waypoints
     * that competitors need to travel in this leg. Due to the dynamics of the wind direction and the mark positions,
     * the result is time dependent. As time point, the middle of the interval defined by the first mark passing starting the
     * leg and the last mark passing finishing the leg is used, where both time points default to "now."
     * 
     * @see #getWindwardDistance(TimePoint, WindLegTypeAndLegBearingCache)
     */
    Distance getWindwardDistance();

    /**
     * Same as {@link #getWindwardDistance()}, but offering the client to use a cache
     */
    Distance getWindwardDistance(WindLegTypeAndLegBearingCache cache);

    Distance getAbsoluteWindwardDistance(WindLegTypeAndLegBearingCache cache);

    /**
     * The middle (traveling half the length) of the course middle line, connecting the center of gravity of the leg's start
     * waypoint's position at time point <code>at</code> and the position of the leg's end waypoint at time point <code>at</code>.
     */
    Position getMiddleOfLeg(TimePoint at);

    /**
     * @param timepoint Used for positions of marks and wind information
     * @return estimated time it takes to complete the leg
     * @throws NotEnoughDataHasBeenAddedException thrown if not enough polar data has been added or polar data service
     * is not available
     * @throws NoWindException no wind available. unable to determine legtypes for given timepoint
     */
    LegTargetTimeInfo getEstimatedTimeAndDistanceToComplete(PolarDataService polarDataService, TimePoint timepoint, MarkPositionAtTimePointCache markPositionCache)
            throws NotEnoughDataHasBeenAddedException, NoWindException;

    /**
     * Computes the windward distance (for upwind/downwind legs) or the along-course distance (for reaching legs) at a reference time point
     * between the leg's start waypoint position and <code>pos</code>. The reference time point is chosen as the middle between the first
     * leg entry and the last leg exit, both defaulting to "now" if no such time point exists.
     */
    Distance getAbsoluteWindwardDistanceFromLegStart(Position pos);

    /**
     * Same as {@link #getAbsoluteWindwardDistanceFromLegStart(Position)}, offering the caller to pass in a cache
     */
    Distance getAbsoluteWindwardDistanceFromLegStart(Position pos, WindLegTypeAndLegBearingCache cache);

    Distance getWindwardDistanceFromLegStart(Position pos, WindLegTypeAndLegBearingCache cache);

    /**
     * Determines an average true wind direction for this leg; it does so by querying the tracked race for
     * wind data at the leg's middle around a reference time point which is defined by the mark passings
     * of the competitors entering and finishing this leg.
     */
    WindWithConfidence<Pair<Position, TimePoint>> getAverageTrueWindDirection();

    /**
     * Computes a reference time point for this leg that is the same for all competitors and that is the middle between
     * the time points of first leg entry and last leg exit. If no competitor has entered the leg, "now" is used as a
     * default. If competitors have entered the leg but none has finished it yet, the middle between first entry and
     * "now" is used.
     */
    TimePoint getReferenceTimePoint();

    /**
     * The leader at the given <code>timePoint</code> in this leg, based on the {@link TrackedRace#getRankingMetric() ranking metric}
     * installed for the race.
     */
    Competitor getLeader(TimePoint timePoint);

    Competitor getLeader(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);
}
