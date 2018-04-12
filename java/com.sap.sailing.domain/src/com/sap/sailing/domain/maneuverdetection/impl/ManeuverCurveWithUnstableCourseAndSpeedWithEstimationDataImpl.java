package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sailing.domain.tracking.impl.ManeuverCurveBoundariesImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl extends ManeuverCurveBoundariesImpl
        implements ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData {

    private final SpeedWithBearing averageSpeedWithBearingBefore;
    private final Duration durationFromPreviousManeuverEndToManeuverStart;
    private final SpeedWithBearing averageSpeedWithBearingAfter;
    private final Duration durationFromManeuverEndToNextManeuverStart;
    private final Distance distanceSailedWithinManeuver;
    private final Distance distanceSailedWithinManeuverTowardMiddleAngleProjection;
    private final Distance distanceSailedIfNotManeuvering;
    private final Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;
    private final int gpsFixesCount;

    public ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl(TimePoint timePointBefore,
            TimePoint timePointAfter, SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees, Speed lowestSpeed,
            SpeedWithBearing averageSpeedWithBearingBefore, Duration durationFromPreviousManeuverEndToManeuverStart,
            SpeedWithBearing averageSpeedWithBearingAfter, Duration durationFromManeuverEndToNextManeuverStart,
            Distance distanceSailedWithinManeuver, Distance distanceSailedWithinManeuverTowardMiddleAngleProjection,
            Distance distanceSailedIfNotManeuvering,
            Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering, int gpsFixesCount) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter, directionChangeInDegrees,
                lowestSpeed);
        this.averageSpeedWithBearingBefore = averageSpeedWithBearingBefore;
        this.durationFromPreviousManeuverEndToManeuverStart = durationFromPreviousManeuverEndToManeuverStart;
        this.averageSpeedWithBearingAfter = averageSpeedWithBearingAfter;
        this.durationFromManeuverEndToNextManeuverStart = durationFromManeuverEndToNextManeuverStart;
        this.distanceSailedWithinManeuver = distanceSailedWithinManeuver;
        this.distanceSailedWithinManeuverTowardMiddleAngleProjection = distanceSailedWithinManeuverTowardMiddleAngleProjection;
        this.distanceSailedIfNotManeuvering = distanceSailedIfNotManeuvering;
        this.distanceSailedTowardMiddleAngleProjectionIfNotManeuvering = distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;
        this.gpsFixesCount = gpsFixesCount;
    }

    @Override
    public SpeedWithBearing getAverageSpeedWithBearingBefore() {
        return averageSpeedWithBearingBefore;
    }

    @Override
    public Duration getDurationFromPreviousManeuverEndToManeuverStart() {
        return durationFromPreviousManeuverEndToManeuverStart;
    }

    @Override
    public SpeedWithBearing getAverageSpeedWithBearingAfter() {
        return averageSpeedWithBearingAfter;
    }

    @Override
    public Duration getDurationFromManeuverEndToNextManeuverStart() {
        return durationFromManeuverEndToNextManeuverStart;
    }

    @Override
    public Distance getDistanceSailedWithinManeuver() {
        return distanceSailedWithinManeuver;
    }

    @Override
    public Distance getDistanceSailedWithinManeuverTowardMiddleAngleProjection() {
        return distanceSailedWithinManeuverTowardMiddleAngleProjection;
    }

    @Override
    public Distance getDistanceSailedIfNotManeuvering() {
        return distanceSailedIfNotManeuvering;
    }

    @Override
    public Distance getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering() {
        return distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;
    }
    
    @Override
    public int getGpsFixesCount() {
        return gpsFixesCount;
    }

}
