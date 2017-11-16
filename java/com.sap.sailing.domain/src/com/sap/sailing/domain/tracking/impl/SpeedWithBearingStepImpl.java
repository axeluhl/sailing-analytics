package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 * @see SpeedWithBearingStep
 *
 */
public class SpeedWithBearingStepImpl implements SpeedWithBearingStep {
    private static final long serialVersionUID = 6541349657098710337L;

    private final TimePoint timePoint;
    private final SpeedWithBearing speedWithBearing;
    private final double courseChangeInDegrees;
    private final double angularVelocityInDegreesPerSecond;

    /**
     * Constructs a speed with bearing step with details about speed, bearing and course change related to the previous
     * step.
     * 
     * @param timePoint
     *            The time point when the step details have been recorded
     * @param speedWithBearing
     *            Speed with bearing at the provided time point
     * @param courseChangeInDegrees
     *            Course change in degrees compared to the previous step. Zero, if this is a first step.
     */
    public SpeedWithBearingStepImpl(TimePoint timePoint, SpeedWithBearing speedWithBearing,
            double courseChangeInDegrees, double angularVelocityInDegreesPerSecond) {
        this.timePoint = timePoint;
        this.speedWithBearing = speedWithBearing;
        this.courseChangeInDegrees = courseChangeInDegrees;
        this.angularVelocityInDegreesPerSecond = angularVelocityInDegreesPerSecond;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

    @Override
    public SpeedWithBearing getSpeedWithBearing() {
        return speedWithBearing;
    }

    @Override
    public double getCourseChangeInDegrees() {
        return courseChangeInDegrees;
    }

    @Override
    public String toString() {
        return "Speed: " + speedWithBearing.getKnots() + " kts, course change: " + courseChangeInDegrees
                + "°, angular velocity: " + angularVelocityInDegreesPerSecond + "°/s, bearing: "
                + speedWithBearing.getBearing().getDegrees() + "°";
    }

    @Override
    public double getAngularVelocityInDegreesPerSecond() {
        return angularVelocityInDegreesPerSecond;
    }
}