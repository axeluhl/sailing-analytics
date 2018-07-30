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

    public static final String POSITION = "position";
    public static final String MARK_PASSING = "markPassing";
    public static final String MAIN_CURVE = "mainCurve";
    public static final String CURVE_WITH_UNSTABLE_COURSE_AND_SPEED = "curveWithUnstableCourseAndSpeed";
    public static final String WIND = "wind";
    public static final String TACKING_COUNT = "tackingCount";
    public static final String JIBING_COUNT = "jibingCount";
    public static final String MANEUVER_STARTS_BY_RUNNING_AWAY_FROM_WIND = "maneuverStartsByRunningAwayFromWind";
    public static final String RELATIVE_BEARING_TO_NEXT_MARK_BEFORE_MANEUVER = "relativeBearingToNextMarkBeforeManeuver";
    public static final String RELATIVE_BEARING_TO_NEXT_MARK_AFTER_MANEUVER = "relativeBearingToNextMarkAfterManeuver";
    public static final String CLOSEST_DISTANCE_TO_MARK = "closestDistanceToMarkInMeters";
    public static final String TARGET_TACK_ANGLE = "targetTackAngleInDegrees";
    public static final String TARGET_JIBE_ANGLE = "targetJibeAngleInDegrees";

    private final ManeuverCurveBoundariesJsonSerializer mainCurveSerializer;
    private final ManeuverCurveBoundariesJsonSerializer curveWithUnstableCourseAndSpeedSerializer;
    private final ManeuverWindJsonSerializer windSerializer;
    private final PositionJsonSerializer positionSerializer;

    public CompleteManeuverCurveWithEstimationDataJsonSerializer(
            ManeuverCurveBoundariesJsonSerializer mainCurveSerializer,
            ManeuverCurveBoundariesJsonSerializer curveWithUnstableCourseAndSpeedSerializer,
            ManeuverWindJsonSerializer windSerializer, PositionJsonSerializer positionSerializer) {
        this.mainCurveSerializer = mainCurveSerializer;
        this.curveWithUnstableCourseAndSpeedSerializer = curveWithUnstableCourseAndSpeedSerializer;
        this.windSerializer = windSerializer;
        this.positionSerializer = positionSerializer;
    }

    @Override
    public JSONObject serialize(CompleteManeuverCurveWithEstimationData maneuverWithEstimationData) {
        final JSONObject result = new JSONObject();
        result.put(POSITION, positionSerializer.serialize(maneuverWithEstimationData.getPosition()));
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
        result.put(CLOSEST_DISTANCE_TO_MARK, maneuverWithEstimationData.getDistanceToClosestMark() == null ? null
                : maneuverWithEstimationData.getDistanceToClosestMark().getMeters());
        result.put(TARGET_TACK_ANGLE, maneuverWithEstimationData.getTargetTackAngleInDegrees());
        result.put(TARGET_JIBE_ANGLE, maneuverWithEstimationData.getTargetJibeAngleInDegrees());
        return result;
    }

}
