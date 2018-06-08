package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public interface HasDetailedManeuverLoss {

    /**
     * Gets the speed before the maneuver beginning.
     */
    Speed getSpeedWithBearingBefore();

    /**
     * Gets the distance which was sailed through the fixes of maneuver curve. The yielded distance is calculated by
     * summation of sub-distances contained between each two consecutive fixes.
     */
    Distance getDistanceSailedWithinManeuver();

    /**
     * Gets the distance of projection toward middle course, which is projected from the line between start and end
     * position of the maneuver curve toward middle course of the maneuver curve.
     */
    Distance getDistanceSailedWithinManeuverTowardMiddleAngleProjection();

    /**
     * Gets the distance which could be sailed by the speed before maneuver start, if the maneuver curve had not been
     * performed.
     */
    Distance getDistanceSailedIfNotManeuvering();

    /**
     * Gets the distance of projection toward middle course which could be sailed by the speed before maneuver curve
     * start, if the maneuver curve had not been performed.
     * 
     * @see #getDistanceSailedWithinManeuverTowardMiddleAngleProjection()
     */
    Distance getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering();

    /**
     * Gets the ratio between {@link #getDistanceSailedWithinManeuver()} and
     * {@link #getDistanceSailedIfNotManeuvering()}.
     */
    default double getRatioBetweenDistanceSailedWithAndWithoutManeuver() {
        return getDistanceSailedWithinManeuver().getMeters() / getDistanceSailedIfNotManeuvering().getMeters();
    }

    /**
     * Gets the difference between {@link #getDistanceSailedIfNotManeuvering()} and
     * {@link #getDistanceSailedWithinManeuver()}.
     */
    default Distance getDistanceLost() {
        return new MeterDistance(
                getDistanceSailedIfNotManeuvering().getMeters() - getDistanceSailedWithinManeuver().getMeters());
    }

    /**
     * Gets the difference between {@link #getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering()} and
     * {@link #getDistanceSailedWithinManeuverTowardMiddleAngleProjection()}.
     */
    default Distance getDistanceLostTowardMiddleAngleProjection() {
        return new MeterDistance(getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering().getMeters()
                - getDistanceSailedWithinManeuverTowardMiddleAngleProjection().getMeters());
    }

    /**
     * Gets the duration lost by maneuver.
     */
    default Duration getDurationLostByManeuver() {
        return new MillisecondsDurationImpl(
                (long) (getDistanceLost().getMeters() / getSpeedWithBearingBefore().getMetersPerSecond() * 1000));
    }

    /**
     * Gets the duration lost by maneuver toward middle angle projection.
     */
    default Duration getDurationLostByManeuverTowardMiddleAngleProjection() {
        return new MillisecondsDurationImpl((long) (getDistanceLostTowardMiddleAngleProjection().getMeters()
                / getSpeedWithBearingBefore().getMetersPerSecond() * 1000));
    }

    /**
     * Gets the ratio between {@link #getDistanceSailedWithinManeuverTowardMiddleAngleProjection()} and
     * {@link #getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering()}
     */
    default double getRatioBetweenDistanceSailedTowardMiddleAngleProjectionWithAndWithoutManeuver() {
        return getDistanceSailedWithinManeuverTowardMiddleAngleProjection().getMeters()
                / getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering().getMeters();
    }

}
