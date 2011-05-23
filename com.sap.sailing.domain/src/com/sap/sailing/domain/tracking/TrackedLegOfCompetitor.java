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
     * direction.
     */
    Distance getWindwardDistanceToGo();

    Speed getAverageVelocityMadeGood();

    Speed getAverageSpeedOverGround();

    Speed getMaximumSpeedOverGround();

    int getNumberOfTacks();

    int getNumberOfJibes();

    int getNumberOfDirectionChanges();

}
