package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.ManeuverMainCurveWithEstimationData;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverMainCurveWithEstimationDataImpl extends ManeuverCurveBoundariesWithDetailedManeuverLossImpl
        implements ManeuverMainCurveWithEstimationData {

    private final TimePoint lowestSpeedTimePoint;
    private final SpeedWithBearing highestSpeed;
    private final TimePoint highestSpeedTimePoint;
    private final TimePoint timePointOfMaxTurningRate;
    private final double maxTurningRateInDegreesPerSecond;
    private final Bearing courseAtMaxTurningRate;
    private final double avgTurningRateInDegreesPerSecond;
    private final int gpsFixesCount;
    private final Duration longestIntervalBetweenTwoFixes;

    public ManeuverMainCurveWithEstimationDataImpl(TimePoint timePointBefore, TimePoint timePointAfter,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees, SpeedWithBearing lowestSpeed, TimePoint lowestSpeedTimePoint,
            SpeedWithBearing highestSpeed, TimePoint highestSpeedTimePoint, TimePoint timePointOfMaxTurningRate,
            double maxTurningRateInDegreesPerSecond, Bearing courseAtMaxTurningRate,
            Distance distanceSailedWithinManeuver, Distance distanceSailedWithinManeuverTowardMiddleAngleProjection,
            Distance distanceSailedIfNotManeuvering, Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering,
            double avgTurningRateInDegreesPerSecond, int gpsFixesCount, Duration longestIntervalBetweenTwoFixes) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter, directionChangeInDegrees,
                lowestSpeed, distanceSailedWithinManeuver, distanceSailedWithinManeuverTowardMiddleAngleProjection,
                distanceSailedIfNotManeuvering, distanceSailedTowardMiddleAngleProjectionIfNotManeuvering);
        this.lowestSpeedTimePoint = lowestSpeedTimePoint;
        this.highestSpeed = highestSpeed;
        this.highestSpeedTimePoint = highestSpeedTimePoint;
        this.timePointOfMaxTurningRate = timePointOfMaxTurningRate;
        this.maxTurningRateInDegreesPerSecond = maxTurningRateInDegreesPerSecond;
        this.courseAtMaxTurningRate = courseAtMaxTurningRate;
        this.avgTurningRateInDegreesPerSecond = avgTurningRateInDegreesPerSecond;
        this.gpsFixesCount = gpsFixesCount;
        this.longestIntervalBetweenTwoFixes = longestIntervalBetweenTwoFixes;
    }

    @Override
    public SpeedWithBearing getLowestSpeed() {
        return (SpeedWithBearing) super.getLowestSpeed();
    }

    @Override
    public TimePoint getLowestSpeedTimePoint() {
        return lowestSpeedTimePoint;
    }

    @Override
    public SpeedWithBearing getHighestSpeed() {
        return highestSpeed;
    }

    @Override
    public TimePoint getHighestSpeedTimePoint() {
        return highestSpeedTimePoint;
    }

    @Override
    public TimePoint getTimePointOfMaxTurningRate() {
        return timePointOfMaxTurningRate;
    }

    @Override
    public double getMaxTurningRateInDegreesPerSecond() {
        return maxTurningRateInDegreesPerSecond;
    }

    @Override
    public Bearing getCourseAtMaxTurningRate() {
        return courseAtMaxTurningRate;
    }

    @Override
    public double getAvgTurningRateInDegreesPerSecond() {
        return avgTurningRateInDegreesPerSecond;
    }

    @Override
    public int getGpsFixesCount() {
        return gpsFixesCount;
    }

    @Override
    public Duration getLongestIntervalBetweenTwoFixes() {
        return longestIntervalBetweenTwoFixes;
    }

}
