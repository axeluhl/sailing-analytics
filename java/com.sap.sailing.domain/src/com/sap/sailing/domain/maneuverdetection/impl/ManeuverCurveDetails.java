package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.ManeuverCurveEnteringAndExitingDetails;
import com.sap.sailing.domain.tracking.impl.ManeuverCurveEnteringAndExitingDetailsImpl;
import com.sap.sse.common.TimePoint;

/**
 * Besides the parent class, this class contains the time point with the maximal angular velocity.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveDetails extends ManeuverCurveEnteringAndExitingDetailsImpl {
    private final TimePoint timePoint;
    private final double maxAngularVelocityInDegreesPerSecond;

    public ManeuverCurveDetails(TimePoint timePointBefore, TimePoint timePointAfter, TimePoint timePoint,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees, double maxAngularVelocityInDegreesPerSecond) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter,
                directionChangeInDegrees);
        this.timePoint = timePoint;
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
     * The maximal angular velocity recorded within the curve which was recorded at {@link #getTimePoint()}.
     * 
     * @return The maximal angular velocity in degrees per second
     */
    public double getMaxAngularVelocityInDegreesPerSecond() {
        return maxAngularVelocityInDegreesPerSecond;
    }
    
    public ManeuverCurveEnteringAndExitingDetails extractEnteringAndExistingDetailsOnly() {
        return new ManeuverCurveEnteringAndExitingDetailsImpl(getTimePointBefore(), getTimePointAfter(), getSpeedWithBearingBefore(), getSpeedWithBearingAfter(), getDirectionChangeInDegrees());
    }
}