package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public interface TrackedLeg extends Serializable {
    Leg getLeg();
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    TrackedRace getTrackedRace();

    /**
     * Determines whether the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
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
     * If the current {@link #getLeg() leg} is +/- {@link TrackedLegImpl#UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the wind's bearing, <code>pos1</code> is projected onto the line crossing <code>pos2</code> in the
     * wind's bearing, and the distance from the projection to <code>pos2</code> is returned. Otherwise, it is assumed
     * that the leg is neither an upwind nor a downwind leg, and hence the along-track distance to <code>mark</code> is
     * returned. The distance returned from this method is always positive. See also {@link #getWindwardDistance}.
     * @param at
     *            the wind estimation is performed for this point in time
     */
    Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode) throws NoWindException;

    /**
     * Same as {@link #getAbsoluteWindwardDistance(Position, Position, TimePoint, WindPositionMode)}, only that a cache
     * for leg types, wind data and leg bearings can be passed to save the effort of redundant calculations of these
     * values.
     */
    Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache);

    /**
     * Same as {@link #getAbsoluteWindwardDistance(Position, Position, TimePoint, WindPositionMode)}, but this method considers the leg's
     * direction and will return a negative distance if <code>pos1</code> is already "ahead" of <code>pos2</code> in the
     * leg's direction, or a positive distance otherwise.
     */
    Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode);

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
    Duration getEstimatedTimeToComplete(PolarDataService polarDataService, TimePoint timepoint) throws NotEnoughDataHasBeenAddedException, NoWindException;

}
