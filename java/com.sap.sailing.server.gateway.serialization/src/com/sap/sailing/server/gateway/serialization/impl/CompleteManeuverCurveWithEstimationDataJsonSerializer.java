package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataJsonSerializer
        implements JsonSerializer<CompleteManeuverCurveWithEstimationData> {

    public static final String MARK_PASSING = "markPassing";
    public static final String MAIN_CURVE = "mainCurve";
    public static final String CURVE_WITH_UNSTABLE_COURSE_AND_SPEED = "curveWithUnstableCourseAndSpeed";
    public static final String WIND = "wind";
    public static final String TACKING_COUNT = "tackingCount";
    public static final String JIBING_COUNT = "jibingCount";
    public static final String MANEUVER_STARTS_BY_RUNNING_AWAY_FROM_WIND = "maneuverStartsByRunningAwayFromWind";
    public static final String RELATIVE_BEARING_TO_NEXT_MARK_BEFORE_MANEUVER = "relativeBearingToNextMarkBeforeManeuver";
    public static final String RELATIVE_BEARING_TO_NEXT_MARK_AFTER_MANEUVER = "relativeBearingToNextMarkAfterManeuver";

    private final ManeuverCurveBoundariesJsonSerializer mainCurveSerializer;
    private final ManeuverCurveBoundariesJsonSerializer curveWithUnstableCourseAndSpeedSerializer;
    private final WindJsonSerializer windSerializer;

    public CompleteManeuverCurveWithEstimationDataJsonSerializer(
            ManeuverCurveBoundariesJsonSerializer mainCurveSerializer,
            ManeuverCurveBoundariesJsonSerializer curveWithUnstableCourseAndSpeedSerializer,
            WindJsonSerializer windSerializer) {
        this.mainCurveSerializer = mainCurveSerializer;
        this.curveWithUnstableCourseAndSpeedSerializer = curveWithUnstableCourseAndSpeedSerializer;
        this.windSerializer = windSerializer;
    }

    @Override
    public JSONObject serialize(CompleteManeuverCurveWithEstimationData maneuverWithEstimationData) {
        final JSONObject result = new JSONObject();
        result.put(MARK_PASSING, maneuverWithEstimationData.isMarkPassing());
        result.put(MAIN_CURVE, mainCurveSerializer.serialize(maneuverWithEstimationData.getMainCurve()));
        result.put(CURVE_WITH_UNSTABLE_COURSE_AND_SPEED, curveWithUnstableCourseAndSpeedSerializer
                .serialize(maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()));
        result.put(WIND, maneuverWithEstimationData.getWind() == null ? null
                : windSerializer.serialize(maneuverWithEstimationData.getWind()));
        result.put(TACKING_COUNT, maneuverWithEstimationData.getTackingCount());
        result.put(JIBING_COUNT, maneuverWithEstimationData.getJibingCount());
        result.put(MANEUVER_STARTS_BY_RUNNING_AWAY_FROM_WIND,
                maneuverWithEstimationData.isManeuverStartsByRunningAwayFromWind());
        result.put(RELATIVE_BEARING_TO_NEXT_MARK_BEFORE_MANEUVER,
                maneuverWithEstimationData.getRelativeBearingToNextMarkBeforeManeuver() == null ? null
                        : maneuverWithEstimationData.getRelativeBearingToNextMarkBeforeManeuver().getDegrees());
        result.put(RELATIVE_BEARING_TO_NEXT_MARK_AFTER_MANEUVER,
                maneuverWithEstimationData.getRelativeBearingToNextMarkAfterManeuver() == null ? null
                        : maneuverWithEstimationData.getRelativeBearingToNextMarkAfterManeuver().getDegrees());
        return result;
    }

}
