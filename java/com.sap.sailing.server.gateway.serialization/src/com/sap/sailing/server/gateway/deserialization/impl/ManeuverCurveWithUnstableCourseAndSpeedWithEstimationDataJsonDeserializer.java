package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveBoundariesWithDetailedManeuverLoss;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer
        extends ManeuverCurveBoundariesWithDetailedManeuverLossJsonDeserializer {

    @Override
    public ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData deserialize(JSONObject object)
            throws JsonDeserializationException {
        ManeuverCurveBoundariesWithDetailedManeuverLoss boundaries = super.deserialize(object);
        Double avgSpeedBeforeInKnots = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.AVERAGE_SPEED_BEFORE_IN_KNOTS);
        Double avgCogBefore = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.AVERAGE_COURSE_BEFORE_IN_DEGREES);
        Double secondsBefore = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.DURATION_FROM_PREVIOUS_MANEUVER_IN_SECONDS);
        Double avgSpeedAfterInKnots = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.AVERAGE_SPEED_AFTER_IN_KNOTS);
        Double avgCogAfter = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.AVERAGE_COURSE_AFTER_IN_DEGREES);
        Double secondsAfter = (Double) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.DURATION_TO_NEXT_MANEUVER_IN_SECONDS);
        Long gpsFixesCountBefore = (Long) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.GPS_FIXES_COUNT_FROM_PREVIOUS_MANEUVER_IN_SECONDS);
        Long gpsFixesCountAfter = (Long) object.get(
                ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.GPS_FIXES_COUNT_TO_NEXT_MANEUVER_IN_SECONDS);
        Long gpsFixesCount = (Long) object
                .get(ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer.GPS_FIXES_COUNT);
        return new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl(boundaries.getTimePointBefore(),
                boundaries.getTimePointAfter(), boundaries.getSpeedWithBearingBefore(),
                boundaries.getSpeedWithBearingAfter(), boundaries.getDirectionChangeInDegrees(),
                boundaries.getLowestSpeed(), convertSpeedWithBearing(avgSpeedBeforeInKnots, avgCogBefore),
                convertDuration(secondsBefore), convertCount(gpsFixesCountBefore),
                convertSpeedWithBearing(avgSpeedAfterInKnots, avgCogAfter), convertDuration(secondsAfter),
                convertCount(gpsFixesCountAfter), boundaries.getDistanceSailedWithinManeuver(),
                boundaries.getDistanceSailedWithinManeuverTowardMiddleAngleProjection(),
                boundaries.getDistanceSailedIfNotManeuvering(),
                boundaries.getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering(), convertCount(gpsFixesCount));
    }

    private SpeedWithBearing convertSpeedWithBearing(Double speedInKnots, Double cog) {
        return speedInKnots == null || cog == null ? null
                : new KnotSpeedWithBearingImpl(speedInKnots, new DegreeBearingImpl(cog));
    }

    private Duration convertDuration(Double seconds) {
        return seconds == null ? null : new MillisecondsDurationImpl((long) (seconds * 1000.0));
    }

    private Integer convertCount(Long count) {
        return count == null ? null : count.intValue();
    }

}
