package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.ManeuverForClassification;

public class ManeuverForClassificationJsonSerializer implements JsonSerializer<ManeuverForClassification> {

    public static final String MANEUVER_TYPE = "type";
    public static final String ABSOLUTE_TOTAL_COURSE_CHANGE_IN_DEGREES = "absManeuverAngle";
    public static final String SPEED_IN_SPEED_OUT_RATIO = "speedInOutRatio";
    public static final String OVERSTEERING_IN_DEGREES = "oversteering";
    public static final String SPEED_LOSS_RATIO = "speedLossRatio";
    public static final String SPEED_GAIN_RATIO = "speedGainRatio";
    public static final String LOWEST_SPEED_VS_EXITING_SPEED_RATIO = "lowestVsExitingSpeedRatio";
    public static final String MAXIMAL_TURNING_RATE_IN_DEGREES_PER_SECOND = "maxTurningRate";
    public static final String DEVIATION_FROM_OPTIMAL_TACK_ANGLE_IN_DEGREES = "deviationTackAngle";
    public static final String DEVIATION_FROM_OPTIMAL_JIBE_ANGLE_IN_DEGREES = "deviationJibeAngle";
    public static final String HIGHEST_ABSOLUTE_DEVIATION_OF_BOATS_COURSE_TO_BEARING_FROM_BOAT_TO_NEXT_WAYPOINT_IN_DEGREES = "highestAbsRelativeBearingToNextWaypoint";
    public static final String MANEUVER_DURATION_IN_SECONDS = "maneuverDuration";
    public static final String MAIN_CURVE_DURATION_IN_SECONDS = "mainCurveDuration";
    public static final String RECOVERY_PHASE_DURATION_IN_SECONDS = "recoveryPhaseDuration";
    public static final String TIME_LOSS_IN_SECONDS = "timeLoss";
    public static final String CLEAN = "clean";
    public static final String MANEUVER_CATEGORY = "category";
    public static final String TWA_BEFORE_IN_DEGREES = "twaBefore";
    public static final String TWA_AFTER_IN_DEGREES = "twaAfter";
    public static final String TWS_IN_KNOTS = "tws";
    public static final String SPEED_BEFORE_IN_KNOTS = "speedBefore";
    public static final String SPEED_AFTER_IN_KNOTS = "speedAfter";

    @Override
    public JSONObject serialize(ManeuverForClassification maneuver) {
        JSONObject json = new JSONObject();
        json.put(MANEUVER_TYPE, maneuver.getManeuverType().name());
        json.put(ABSOLUTE_TOTAL_COURSE_CHANGE_IN_DEGREES, maneuver.getAbsoluteTotalCourseChangeInDegrees());
        json.put(SPEED_IN_SPEED_OUT_RATIO, maneuver.getSpeedInSpeedOutRatio());
        json.put(OVERSTEERING_IN_DEGREES, maneuver.getOversteeringInDegrees());
        json.put(SPEED_LOSS_RATIO, maneuver.getSpeedLossRatio());
        json.put(SPEED_GAIN_RATIO, maneuver.getSpeedGainRatioWithinMainCurve());
        json.put(LOWEST_SPEED_VS_EXITING_SPEED_RATIO, maneuver.getLowestSpeedVsExitingSpeedRatio());
        json.put(MAXIMAL_TURNING_RATE_IN_DEGREES_PER_SECOND, maneuver.getMaximalTurningRateInDegreesPerSecond());
        json.put(DEVIATION_FROM_OPTIMAL_TACK_ANGLE_IN_DEGREES, maneuver.getDeviationFromOptimalTackAngleInDegrees());
        json.put(DEVIATION_FROM_OPTIMAL_JIBE_ANGLE_IN_DEGREES, maneuver.getDeviationFromOptimalJibeAngleInDegrees());
        json.put(HIGHEST_ABSOLUTE_DEVIATION_OF_BOATS_COURSE_TO_BEARING_FROM_BOAT_TO_NEXT_WAYPOINT_IN_DEGREES,
                maneuver.getHighestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees());
        json.put(MANEUVER_DURATION_IN_SECONDS, maneuver.getManeuverDurationInSeconds());
        json.put(MAIN_CURVE_DURATION_IN_SECONDS, maneuver.getMainCurveDurationInSeconds());
        json.put(RECOVERY_PHASE_DURATION_IN_SECONDS, maneuver.getRecoveryPhaseDurationInSeconds());
        json.put(TIME_LOSS_IN_SECONDS, maneuver.getTimeLossInSeconds());
        json.put(CLEAN, maneuver.isClean());
        json.put(MANEUVER_CATEGORY, maneuver.getManeuverCategory().name());
        json.put(TWA_BEFORE_IN_DEGREES, maneuver.getTwaBeforeInDegrees());
        json.put(TWA_AFTER_IN_DEGREES, maneuver.getTwaAfterInDegrees());
        json.put(TWS_IN_KNOTS, maneuver.getTwsInKnots());
        json.put(SPEED_BEFORE_IN_KNOTS, maneuver.getSpeedBeforeInKnots());
        json.put(SPEED_AFTER_IN_KNOTS, maneuver.getSpeedAfterInKnots());
        return json;
    }

}
