package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;

public interface TrackedLegOfCompetitor {
    Leg getLeg();

    Competitor getCompetitor();

    /**
     * How long did it take the competitor to complete this {@link #getLeg() leg}? If the competitor hasn't finished the
     * leg yet, -1 is returned.
     */
    long getTimeInMilliSeconds();

    /**
     * The distance over ground traveled by the competitor in this leg
     */
    Distance getDistanceTraveled();

    /**
     * Estimates how much the competitor still has to go to the end waypoint of this leg, projected onto the wind
     * direction. If the competitor already finished this leg, a zero, non-<code>null</code> distance will result.
     */
    Distance getWindwardDistanceToGo();

    Speed getAverageVelocityMadeGood();

    Speed getAverageSpeedOverGround();

    /**
     * @return <code>null</code> if the competitor hasn't started this leg yet
     */
    Speed getMaximumSpeedOverGround();

    int getNumberOfTacks();

    int getNumberOfJibes();

    int getNumberOfDirectionChanges();

    /**
     * Computes the competitor's rank within this leg. If the competitor has already finished this leg, the rank is
     * determined by comparing to all other competitors that also finished this leg. If not yet finished, the rank is
     * i+j+1 where i is the number of competitors that already finished the leg, and j is the number of competitors
     * whose wind-projected distance to the leg's end waypoint is shorter than that of <code>competitor</code>.
     * <p>
     * 
     * The wind projection is only an approximation of a more exact "advantage line" and in particular doesn't
     * account for crossing the lay line.
     */
    int getRank();
    
}
