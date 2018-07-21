package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveBoundariesJsonSerializer implements JsonSerializer<ManeuverCurveBoundaries> {

    public static final String TIME_POINT_BEFORE = "unixTimeBefore";
    public static final String SPEED_BEFORE_IN_KNOTS = "speedBeforeInKnots";
    public static final String COG_BEFORE_IN_TRUE_DEGREES = "cogBeforeInTrueDegrees";
    public static final String TIME_POINT_AFTER = "unixTimeAfter";
    public static final String SPEED_AFTER_IN_KNOTS = "speedAfterInKnots";
    public static final String COG_AFTER_IN_TRUE_DEGREES = "cogAfterInTrueDegrees";
    public static final String DIRECTION_CHANGE_IN_DEGREES = "directionChangeInDegrees";
    public static final String DURATION_IN_MILLIS = "durationInMillis";
    public static final String LOWEST_SPEED_IN_KNOTS = "lowestSpeedInKnots";

    @Override
    public JSONObject serialize(ManeuverCurveBoundaries curveBoundaries) {
        JSONObject result = new JSONObject();
        result.put(TIME_POINT_BEFORE, curveBoundaries.getTimePointBefore().asMillis());
        result.put(SPEED_BEFORE_IN_KNOTS, curveBoundaries.getSpeedWithBearingBefore() == null ? null
                : curveBoundaries.getSpeedWithBearingBefore().getKnots());
        result.put(COG_BEFORE_IN_TRUE_DEGREES, curveBoundaries.getSpeedWithBearingBefore() == null ? null
                : curveBoundaries.getSpeedWithBearingBefore().getBearing().getDegrees());
        result.put(TIME_POINT_AFTER, curveBoundaries.getTimePointAfter().asMillis());
        result.put(SPEED_AFTER_IN_KNOTS, curveBoundaries.getSpeedWithBearingAfter() == null ? null
                : curveBoundaries.getSpeedWithBearingAfter().getKnots());
        result.put(COG_AFTER_IN_TRUE_DEGREES, curveBoundaries.getSpeedWithBearingAfter() == null ? null
                : curveBoundaries.getSpeedWithBearingAfter().getBearing().getDegrees());
        result.put(DIRECTION_CHANGE_IN_DEGREES, curveBoundaries.getDirectionChangeInDegrees());
        result.put(DURATION_IN_MILLIS, curveBoundaries.getTimePointBefore().until(curveBoundaries.getTimePointAfter()).asMillis());
        result.put(LOWEST_SPEED_IN_KNOTS, curveBoundaries.getLowestSpeed().getKnots());
        return result;
    }

}
