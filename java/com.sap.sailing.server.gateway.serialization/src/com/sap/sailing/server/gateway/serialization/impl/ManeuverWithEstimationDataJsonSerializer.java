package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWithEstimationDataJsonSerializer implements JsonSerializer<ManeuverWithEstimationData> {

    public static final String MANEUVER_TYPE = "maneuverType";
    public static final String NEW_TACK = "newTack";
    public static final String POSITION_AND_TIME = "positionAndTime";
    public static final String MAX_ANGULAR_VELOCITY_IN_DEGREES_PER_SECOND = "maxAngularVelocityInDegreesPerSecond";
    public static final String MANEUVER_LOSS_IN_METERS = "maneuverLossInMeters";
    public static final String MAIN_CURVE_BOUNDARIES = "mainCurveBoundaries";
    public static final String MANEUVER_BOUNDARIES = "maneuverBoundaries";
    public static final String WIND = "wind";
    public static final String HIGHEST_SPEED_WITHIN_MAIN_CURVE_IN_KNOTS = "highestSpeedWithinMainCurveInKnots";
    public static final String LOWEST_SPEED_WITHIN_MAIN_CURVE_IN_KNOTS = "lowestSpeedWithinMainCurveInKnots";
    public final static String AVERAGE_SPEED_BEFORE_IN_KNOTS = "averageSpeedBeforeInKnots";
    public final static String AVERAGE_SPEED_AFTER_IN_KNOTS = "averageSpeedAfterInKnots";
    public final static String AVERAGE_COURSE_BEFORE_IN_DEGREES = "averageCourseBeforeInDegrees";
    public final static String AVERAGE_COURSE_AFTER_IN_DEGREES = "averageCourseAfterInDegrees";
    public final static String DURATION_FROM_PREVIOUS_MANEUVER_IN_SECONDS = "durationFromPreviousManeuverInSeconds";
    public final static String DURATION_TO_NEXT_MANEUVER_IN_SECONDS = "durationToNextManeuverInSeconds";

    private final ManeuverCurveEnteringAndExitingDetailsJsonSerializer maneuverCurveEnteringAndExitingDetailsJsonSerializer;
    private final ManeuverWindJsonSerializer windJsonSerializer;
    private final GPSFixJsonSerializer gpsFixSerializer;

    public ManeuverWithEstimationDataJsonSerializer(GPSFixJsonSerializer gpsFixSerializer,
            ManeuverCurveEnteringAndExitingDetailsJsonSerializer maneuverCurveEnteringAndExitingDetailsJsonSerializer,
            ManeuverWindJsonSerializer windJsonSerializer) {
        this.gpsFixSerializer = gpsFixSerializer;
        this.maneuverCurveEnteringAndExitingDetailsJsonSerializer = maneuverCurveEnteringAndExitingDetailsJsonSerializer;
        this.windJsonSerializer = windJsonSerializer;
    }

    @Override
    public JSONObject serialize(ManeuverWithEstimationData maneuverWithEstimationData) {
        Maneuver maneuver = maneuverWithEstimationData.getManeuver();
        final JSONObject result = new JSONObject();
        result.put(MANEUVER_TYPE, maneuver.getType() == null ? null : maneuver.getType().name());
        result.put(NEW_TACK, maneuver.getNewTack() == null ? null : maneuver.getNewTack().name());
        result.put(MAX_ANGULAR_VELOCITY_IN_DEGREES_PER_SECOND, maneuver.getMaxAngularVelocityInDegreesPerSecond());
        result.put(MANEUVER_LOSS_IN_METERS,
                maneuver.getManeuverLoss() == null ? null : maneuver.getManeuverLoss().getMeters());
        result.put(POSITION_AND_TIME, gpsFixSerializer.serialize(maneuver));
        result.put(MAIN_CURVE_BOUNDARIES,
                maneuverCurveEnteringAndExitingDetailsJsonSerializer.serialize(maneuver.getMainCurveBoundaries()));
        result.put(MANEUVER_BOUNDARIES, maneuverCurveEnteringAndExitingDetailsJsonSerializer
                .serialize(maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()));
        result.put(WIND, maneuverWithEstimationData.getWind() == null ? null
                : windJsonSerializer.serialize(maneuverWithEstimationData.getWind()));
        result.put(HIGHEST_SPEED_WITHIN_MAIN_CURVE_IN_KNOTS,
                maneuverWithEstimationData.getHighestSpeedWithinMainCurve() == null ? null
                        : maneuverWithEstimationData.getHighestSpeedWithinMainCurve().getKnots());
        result.put(LOWEST_SPEED_WITHIN_MAIN_CURVE_IN_KNOTS,
                maneuverWithEstimationData.getLowestSpeedWithinMainCurve() == null ? null
                        : maneuverWithEstimationData.getLowestSpeedWithinMainCurve().getKnots());
        result.put(AVERAGE_SPEED_BEFORE_IN_KNOTS, maneuverWithEstimationData.getAverageSpeedWithBearingBefore() == null
                ? null : maneuverWithEstimationData.getAverageSpeedWithBearingBefore().getKnots());
        result.put(AVERAGE_COURSE_BEFORE_IN_DEGREES,
                maneuverWithEstimationData.getAverageSpeedWithBearingBefore() == null ? null
                        : maneuverWithEstimationData.getAverageSpeedWithBearingBefore().getBearing().getDegrees());
        result.put(DURATION_FROM_PREVIOUS_MANEUVER_IN_SECONDS,
                maneuverWithEstimationData.getDurationFromPreviousManeuverEndToManeuverStart() == null ? null
                        : maneuverWithEstimationData.getDurationFromPreviousManeuverEndToManeuverStart().asSeconds());
        result.put(AVERAGE_SPEED_AFTER_IN_KNOTS, maneuverWithEstimationData.getAverageSpeedWithBearingAfter() == null
                ? null : maneuverWithEstimationData.getAverageSpeedWithBearingAfter().getKnots());
        result.put(AVERAGE_COURSE_AFTER_IN_DEGREES, maneuverWithEstimationData.getAverageSpeedWithBearingAfter() == null
                ? null : maneuverWithEstimationData.getAverageSpeedWithBearingAfter().getBearing().getDegrees());
        result.put(DURATION_TO_NEXT_MANEUVER_IN_SECONDS,
                maneuverWithEstimationData.getDurationFromManeuverEndToNextManeuverStart() == null ? null
                        : maneuverWithEstimationData.getDurationFromManeuverEndToNextManeuverStart().asSeconds());
        return result;
    }

}
