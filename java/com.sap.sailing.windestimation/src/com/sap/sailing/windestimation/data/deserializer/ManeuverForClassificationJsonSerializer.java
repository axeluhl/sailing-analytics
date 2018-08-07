package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.ManeuverForClassification;

public class ManeuverForClassificationJsonSerializer implements JsonSerializer<ManeuverForClassification> {

    public static final String MANEUVER_TYPE = "maneuverType";
    public static final String ABSOLUTE_TOTAL_COURSE_CHANGE_IN_DEGREES = "absoluteTotalCourseChangeInDegrees";
    public static final String SPEED_IN_SPEED_OUT_RATIO = "speedInSpeedOutRatio";
    public static final String OVERSTEERING_IN_DEGREES = "oversteeringInDegrees";
    public static final String SPEED_LOSS_RATIO = "speedLossRatio";
    public static final String SPEED_GAIN_RATIO = "speedGainRatio";
    public static final String MAXIMAL_TURNING_RATE_IN_DEGREES_PER_SECOND = "maximalTurningRateInDegreesPerSecond";
    public static final String DEVIATION_FROM_OPTIMAL_TACK_ANGLE_IN_DEGREES = "deviationFromOptimalTackAngleInDegrees";
    public static final String DEVIATION_FROM_OPTIMAL_JIBE_ANGLE_IN_DEGREES = "deviationFromOptimalJibeAngleInDegrees";
    public static final String HIGHEST_ABSOLUTE_DEVIATION_OF_BOATS_COURSE_TO_BEARING_FROM_BOAT_TO_NEXT_WAYPOINT_IN_DEGREES = "highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees";
    public static final String MANEUVER_DURATION_IN_SECONDS = "maneuverDurationInSeconds";
    public static final String MAIN_CURVE_DURATION_IN_SECONDS = "mainCurveDurationInSeconds";
    public static final String RECOVERY_PHASE_DURATION_IN_SECONDS = "recoveryPhaseDurationInSeconds";
    public static final String TIME_LOSS_IN_SECONDS = "timeLossInSeconds";
    public static final String CLEAN = "clean";
    public static final String MANEUVER_CATEGORY = "maneuverCategory";

    @Override
    public JSONObject serialize(ManeuverForClassification maneuver) {
        JSONObject json = new JSONObject();
        json.put(MANEUVER_TYPE, maneuver.getManeuverType().name());
        json.put(ABSOLUTE_TOTAL_COURSE_CHANGE_IN_DEGREES, maneuver.getAbsoluteTotalCourseChangeInDegrees());
        json.put(SPEED_IN_SPEED_OUT_RATIO, maneuver.getSpeedInSpeedOutRatio());
        json.put(OVERSTEERING_IN_DEGREES, maneuver.getOversteeringInDegrees());
        json.put(SPEED_LOSS_RATIO, maneuver.getSpeedLossRatio());
        json.put(SPEED_GAIN_RATIO, maneuver.getSpeedGainRatio());
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
        return json;
    }

}
