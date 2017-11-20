package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.TimePoint;

/**
 * Besides the parent class, this class contains the time point with the maximal angular velocity. Additionally, the
 * total course change of the curve which is calculated throughout the iteration of bearing steps of the curve is also
 * available.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CurveDetails extends CurveEnteringAndExitingDetails {
    private final TimePoint timePoint;
    private final double totalChangeInDegrees;
    private final double maxAngularVelocityInDegreesPerSecond;

    public CurveDetails(TimePoint timePointBefore, TimePoint timePointAfter, TimePoint timePoint,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double totalCourseChangeInDegrees, double maxAngularVelocityInDegreesPerSecond) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter);
        this.timePoint = timePoint;
        this.totalChangeInDegrees = totalCourseChangeInDegrees;
        this.maxAngularVelocityInDegreesPerSecond = maxAngularVelocityInDegreesPerSecond;
    }

    /**
     * Gets the computed time point of the corresponding curve. The time point refers to a position within maneuver,
     * where the highest course change has been recorded.
     * 
     * @return The computed maneuver time point
     */
    public TimePoint getTimePoint() {
        return timePoint;
    }

    /**
     * Gets the total course change performed within the curve in degrees. The port side course changes are negative.
     * 
     * @return The total course change in degrees
     */
    public double getTotalCourseChangeInDegrees() {
        return totalChangeInDegrees;
    }

    /**
     * The maximal angular velocity recorded within the curve which was recorded at {@link #getTimePoint()}.
     * 
     * @return The maximal angular velocity in degrees per second
     */
    public double getMaxAngularVelocityInDegreesPerSecond() {
        return maxAngularVelocityInDegreesPerSecond;
    }
}