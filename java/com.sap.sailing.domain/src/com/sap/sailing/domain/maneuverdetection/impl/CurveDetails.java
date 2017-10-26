package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.TimePoint;

/**
 * Besides the parent class, the time point is contained where highest course change was recorded within the curve.
 * Additionally, the total course change of the curve which is calculated throughout the iteration of bearing steps
 * of the curve is also available.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
class CurveDetails extends CurveEnterindAndExitingDetails {
    private final TimePoint timePoint;
    private final double totalChangeInDegrees;

    public CurveDetails(TimePoint timePointBefore, TimePoint timePointAfter, TimePoint timePoint,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double totalCourseChangeInDegrees) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter);
        this.timePoint = timePoint;
        this.totalChangeInDegrees = totalCourseChangeInDegrees;
    }

    /**
     * Gets the computed time point of the corresponding curve. The time point refers to a position within
     * maneuver, where the highest course change has been recorded.
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
}