package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer
        extends ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer {

    public final static String AVERAGE_SPEED_BEFORE_IN_KNOTS = "averageSpeedBeforeInKnots";
    public final static String AVERAGE_SPEED_AFTER_IN_KNOTS = "averageSpeedAfterInKnots";
    public final static String AVERAGE_COURSE_BEFORE_IN_DEGREES = "averageCourseBeforeInDegrees";
    public final static String AVERAGE_COURSE_AFTER_IN_DEGREES = "averageCourseAfterInDegrees";
    public final static String DURATION_FROM_PREVIOUS_MANEUVER_IN_SECONDS = "durationFromPreviousManeuverInSeconds";
    public final static String DURATION_TO_NEXT_MANEUVER_IN_SECONDS = "durationToNextManeuverInSeconds";

    @Override
    public JSONObject serialize(ManeuverCurveBoundaries curveBoundaries) {
        JSONObject result = super.serialize(curveBoundaries);
        if (curveBoundaries instanceof ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData) {
            ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curve = (ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData) curveBoundaries;
            result.put(AVERAGE_SPEED_BEFORE_IN_KNOTS, curve.getAverageSpeedWithBearingBefore() == null ? null
                    : curve.getAverageSpeedWithBearingBefore().getKnots());
            result.put(AVERAGE_COURSE_BEFORE_IN_DEGREES, curve.getAverageSpeedWithBearingBefore() == null ? null
                    : curve.getAverageSpeedWithBearingBefore().getBearing().getDegrees());
            result.put(DURATION_FROM_PREVIOUS_MANEUVER_IN_SECONDS,
                    curve.getDurationFromPreviousManeuverEndToManeuverStart() == null ? null
                            : curve.getDurationFromPreviousManeuverEndToManeuverStart().asSeconds());
            result.put(AVERAGE_SPEED_AFTER_IN_KNOTS, curve.getAverageSpeedWithBearingAfter() == null ? null
                    : curve.getAverageSpeedWithBearingAfter().getKnots());
            result.put(AVERAGE_COURSE_AFTER_IN_DEGREES, curve.getAverageSpeedWithBearingAfter() == null ? null
                    : curve.getAverageSpeedWithBearingAfter().getBearing().getDegrees());
            result.put(DURATION_TO_NEXT_MANEUVER_IN_SECONDS,
                    curve.getDurationFromManeuverEndToNextManeuverStart() == null ? null
                            : curve.getDurationFromManeuverEndToNextManeuverStart().asSeconds());
        }
        return result;
    }

}
