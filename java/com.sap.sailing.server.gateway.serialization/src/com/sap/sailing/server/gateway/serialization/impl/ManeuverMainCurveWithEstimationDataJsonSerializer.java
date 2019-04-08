package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.maneuverdetection.ManeuverMainCurveWithEstimationData;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverMainCurveWithEstimationDataJsonSerializer
        extends ManeuverCurveBoundariesWithDetailedManeuverLossJsonSerializer {

    public static final String AVG_TURNING_RATE_IN_DEGREES_PER_SECOND = "avgTurningRateInDegreesPerSecond";
    public static final String MAX_TURNING_RATE_IN_DEGREES_PER_SECOND = "maxTurningRateInDegreesPerSecond";
    public static final String HIGHEST_SPEED_IN_KNOTS = "highestSpeedInKnots";
    public static final String COURSE_AT_LOWEST_SPEED = "courseAtLowestSpeed";
    public static final String COURSE_AT_HIGHEST_SPEED = "courseAtHighestSpeed";
    public static final String LOWEST_SPEED_TIMEPOINT = "lowestSpeedTimePoint";
    public static final String HIGHEST_SPEED_TIMEPOINT = "highestSpeedTimePoint";
    public static final String COURSE_AT_MAX_TURNING_RATE = "courseAtMaxTurningRate";
    public static final String MAX_TURNING_RATE_TIMEPOINT = "maxTurningRateTimePoint";
    public static final String GPS_FIXES_COUNT = "gpsFixesCount";
    public static final String LONGEST_INTERVAL_BETWEEN_TWO_FIXES = "longestIntervalBetweenTwoFixesInSeconds";

    @Override
    public JSONObject serialize(ManeuverCurveBoundaries maneuverBoundaries) {
        JSONObject result = super.serialize(maneuverBoundaries);
        if (maneuverBoundaries instanceof ManeuverMainCurveWithEstimationData) {
            ManeuverMainCurveWithEstimationData mainCurve = (ManeuverMainCurveWithEstimationData) maneuverBoundaries;
            result.put(AVG_TURNING_RATE_IN_DEGREES_PER_SECOND, mainCurve.getAvgTurningRateInDegreesPerSecond());
            result.put(MAX_TURNING_RATE_IN_DEGREES_PER_SECOND, mainCurve.getMaxTurningRateInDegreesPerSecond());
            result.put(HIGHEST_SPEED_IN_KNOTS, mainCurve.getHighestSpeed().getKnots());
            result.put(COURSE_AT_LOWEST_SPEED, mainCurve.getLowestSpeed().getBearing().getDegrees());
            result.put(COURSE_AT_HIGHEST_SPEED, mainCurve.getHighestSpeed().getBearing().getDegrees());
            result.put(LOWEST_SPEED_TIMEPOINT, mainCurve.getLowestSpeedTimePoint().asMillis());
            result.put(HIGHEST_SPEED_TIMEPOINT, mainCurve.getHighestSpeedTimePoint().asMillis());
            result.put(COURSE_AT_MAX_TURNING_RATE, mainCurve.getCourseAtMaxTurningRate().getDegrees());
            result.put(MAX_TURNING_RATE_TIMEPOINT, mainCurve.getTimePointOfMaxTurningRate().asMillis());
            result.put(GPS_FIXES_COUNT, mainCurve.getGpsFixesCount());
            result.put(LONGEST_INTERVAL_BETWEEN_TWO_FIXES, mainCurve.getLongestIntervalBetweenTwoFixes().asSeconds());
        }
        return result;
    }
}
