package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;

public interface TrackedLegOfCompetitor {
    Leg getLeg();

    Competitor getCompetitor();

    /**
     * How long did it take the competitor to complete this {@link #getLeg() leg}? If the competitor hasn't finished the
     * leg yet, -1 is returned.
     */
    long getTimeInMilliSeconds(TimePoint timePoint);

    /**
     * The distance over ground traveled by the competitor in this leg up to <code>timePoint</code>. If
     * <code>timePoint</code> is before the competitor started this leg, a {@link Distance#NULL zero} distance is
     * returned. If the <code>timePoint</code> is after the time point at which the competitor finished this leg (if the
     * respective mark passing has already been received), the total distance traveled in this leg is returned. If the
     * time point is after the last fix but the competitor hasn't finished the leg yet, the distance traveled up to the
     * position at which the competitor is estimated to be at <code>timePoint</code> is used.
     */
    Distance getDistanceTraveled(TimePoint timePoint);

    /**
     * Estimates how much the competitor still has to go to the end waypoint of this leg, projected onto the wind
     * direction. If the competitor already finished this leg, a zero, non-<code>null</code> distance will result.
     * If the competitor hasn't started the leg yet, the full leg distance is returned.
     * 
     * @throws NoWindException
     *             thrown in case no wind direction is known
     */
    Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException;

    Speed getAverageVelocityMadeGood(TimePoint timePoint) throws NoWindException;

    /**
     * Computes the competitor's average speed over ground for this leg from the beginning of the leg up to time
     * <code>timePoint</code> or at the time of the last event received for the race in case <code>timePoint</code> is
     * after the time when the last fix for this competitor was received. If the competitor already completed the leg at
     * <code>timePoint</code> and the respective mark passing event was already received, the average speed over ground
     * for the entire leg (and no further) is computed.
     */
    Speed getAverageSpeedOverGround(TimePoint timePoint);

    /**
     * @return <code>null</code> if the competitor hasn't started this leg yet
     */
    Speed getMaximumSpeedOverGround(TimePoint timePoint);

    int getNumberOfTacks(TimePoint timePoint);

    int getNumberOfJibes(TimePoint timePoint);

    int getNumberOfDirectionChanges(TimePoint timePoint);

    /**
     * Computes the competitor's rank within this leg. If the competitor has already finished this leg at
     * <code>timePoint</code>, the rank is determined by comparing to all other competitors that also finished this leg.
     * If not yet finished, the rank is i+j+1 where i is the number of competitors that already finished the leg, and j
     * is the number of competitors whose wind-projected distance to the leg's end waypoint is shorter than that of
     * <code>competitor</code>.
     * <p>
     * 
     * The wind projection is only an approximation of a more exact "advantage line" and in particular doesn't account
     * for crossing the lay line.
     */
    int getRank(TimePoint timePoint);

    /**
     * Computes the gap in seconds to the leader / winner of this leg. Returns <code>null</code> in case this leg's
     * competitor hasn't started the leg yet.
     */
    Double getGapToLeaderInSeconds(TimePoint timePoint) throws NoWindException;

    boolean hasStartedLeg(TimePoint timePoint);
    
    boolean hasFinishedLeg(TimePoint timePoint);

    /**
     * Returns <code>null</code> in case this leg's competitor hasn't started the leg yet.
     */
    Speed getVelocityMadeGood(TimePoint timePoint) throws NoWindException;

    /**
     * Returns <code>null</code> in case this leg's competitor hasn't started the leg yet.
     */
    Double getEstimatedTimeToNextMarkInSeconds(TimePoint timePoint) throws NoWindException;

    SpeedWithBearing getSpeedOverGround(TimePoint at);

    /**
     * Computes the distance along the wind track to the wind-projected position of the race's
     * overall leader. If leader and competitor are in the same leg, this is simply the windward
     * distance. If the leader is already one or more legs ahead, it's the competitor's winward
     * distance to go plus the windward distance between the marks of all legs that the leader
     * completed after this competitor's leg plus the windard distance between the leader and
     * the leader's leg's start.
     */
    Distance getWindwardDistanceToOverallLeader(TimePoint timePoint) throws NoWindException;

}
