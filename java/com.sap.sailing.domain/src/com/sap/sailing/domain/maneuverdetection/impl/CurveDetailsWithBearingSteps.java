package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.SpeedWithBearingStepsIterable;
import com.sap.sse.common.TimePoint;

/**
 * Besides its parent class, this class reveals speed and bearing steps of the curve.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
class CurveDetailsWithBearingSteps extends CurveDetails {
    private final SpeedWithBearingStepsIterable speedWithBearingSteps;

    public CurveDetailsWithBearingSteps(TimePoint timepointBefore, TimePoint timepointAfter, TimePoint timepoint,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double totalCourseChangeInDegrees, double maxAngularVelocityInDegreesPerSecond,
            SpeedWithBearingStepsIterable speedWithBearingSteps) {
        super(timepointBefore, timepointAfter, timepoint, speedWithBearingBefore, speedWithBearingAfter,
                totalCourseChangeInDegrees, maxAngularVelocityInDegreesPerSecond);
        this.speedWithBearingSteps = speedWithBearingSteps;
    }

    /**
     * Gets the list of bearing steps which was used for computation of curve details.
     * 
     * @return The bearing steps of the curve
     */
    public SpeedWithBearingStepsIterable getSpeedWithBearingSteps() {
        return speedWithBearingSteps;
    }
}