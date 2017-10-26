package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.List;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sse.common.TimePoint;

/**
 * Besides its parent class, this class reveals speed and bearing steps of the curve.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
class CurveDetailsWithBearingSteps extends CurveDetails {
    private final List<SpeedWithBearingStep> speedWithBearingSteps;

    public CurveDetailsWithBearingSteps(TimePoint timepointBefore, TimePoint timepointAfter, TimePoint timepoint,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double totalCourseChangeInDegrees, List<SpeedWithBearingStep> speedWithBearingSteps) {
        super(timepointBefore, timepointAfter, timepoint, speedWithBearingBefore, speedWithBearingAfter,
                totalCourseChangeInDegrees);
        this.speedWithBearingSteps = speedWithBearingSteps;
    }

    /**
     * Gets the list of bearing steps which was used for computation of curve details.
     * 
     * @return The bearing steps of the curve
     */
    public List<SpeedWithBearingStep> getSpeedWithBearingSteps() {
        return speedWithBearingSteps;
    }
}