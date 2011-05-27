package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;

public interface TrackedLegOfCompetitor {
    Leg getLeg();

    Competitor getCompetitor();

    /**
     * How long did it take the competitor to complete this {@link #getLeg() leg}? If the competitor hasn't finished the
     * leg yet, -1 is returned.
     */
    long getTimeInMilliSeconds();

    /**
     * The distance over ground traveled by the competitor in this leg up to <code>timePoint</code>. If
     * <code>timePoint</code> is before the competitor started this leg, a {@link Distance#NULL zero} distance
     * is returned. If the <code>timePoint</code> is after the time point at which the competitor finished this
     * leg, the total distance traveled in this leg is returned. If the time point is after the last fix but
     * the competitor hasn't finished the leg yet, the distance traveled up to the position at which the
     * competitor is estimated to be at <code>timePoint</code> is used.
     */
    Distance getDistanceTraveled(TimePoint timePoint);

    /**
     * Estimates how much the competitor still has to go to the end waypoint of this leg, projected onto the wind
     * direction. If the competitor already finished this leg, a zero, non-<code>null</code> distance will result.
     * @param timePoint TODO
     * 
     * @throws NoWindException
     *             thrown in case no wind direction is known
     */
    Distance getWindwardDistanceToGo(TimePoint timePoint) throws NoWindException;

    Speed getAverageVelocityMadeGood(TimePoint timePoint);

    Speed getAverageSpeedOverGround(TimePoint timePoint);

    /**
     * @return <code>null</code> if the competitor hasn't started this leg yet
     */
    Speed getMaximumSpeedOverGround();

    int getNumberOfTacks(TimePoint timePoint);

    int getNumberOfJibes(TimePoint timePoint);

    int getNumberOfDirectionChanges(TimePoint timePoint);

    /**
     * Computes the competitor's rank within this leg. If the competitor has already finished this leg, the rank is
     * determined by comparing to all other competitors that also finished this leg. If not yet finished, the rank is
     * i+j+1 where i is the number of competitors that already finished the leg, and j is the number of competitors
     * whose wind-projected distance to the leg's end waypoint is shorter than that of <code>competitor</code>.
     * <p>
     * 
     * The wind projection is only an approximation of a more exact "advantage line" and in particular doesn't
     * account for crossing the lay line.
     * @param timePoint TODO
     */
    int getRank(TimePoint timePoint);

    double getGapToLeaderInSeconds(TimePoint timePoint);

    boolean hasFinishedLeg(TimePoint timePoint);

    Speed getVelocityMadeGood(TimePoint timePoint) throws NoWindException;
    
}
