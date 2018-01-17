package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.SpeedWithBearingStepsIterable;
import com.sap.sse.common.TimePoint;

/**
 * Besides its parent class, this class reveals speed and bearing steps of the maneuver curve.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveDetailsWithBearingSteps extends ManeuverCurveDetails {

    private final SpeedWithBearingStepsIterable speedWithBearingSteps;
    
    public ManeuverCurveDetailsWithBearingSteps(TimePoint timePointBefore, TimePoint timePointAfter,
            TimePoint timePoint, SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees, double maxAngularVelocityInDegreesPerSecond, SpeedWithBearingStepsIterable speedWithBearingSteps) {
        super(timePointBefore, timePointAfter, timePoint, speedWithBearingBefore, speedWithBearingAfter,
                directionChangeInDegrees, maxAngularVelocityInDegreesPerSecond);
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
