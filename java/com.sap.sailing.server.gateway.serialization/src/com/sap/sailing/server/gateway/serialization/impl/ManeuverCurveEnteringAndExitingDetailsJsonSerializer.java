package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveEnteringAndExitingDetailsJsonSerializer implements JsonSerializer<ManeuverCurveBoundaries> {

    public static final String SPEED_BEFORE_IN_KNOTS = "speedBeforeInKnots";
    public static final String COG_BEFORE_IN_TRUE_DEGREES = "cogBeforeInTrueDegrees";
    public static final String SPEED_AFTER_IN_KNOTS = "speedAfterInKnots";
    public static final String COG_AFTER_IN_TRUE_DEGREES = "cogAfterInTrueDegrees";
    public static final String DIRECTION_CHANGE_IN_DEGREES = "directionChangeInDegrees";

    @Override
    public JSONObject serialize(ManeuverCurveBoundaries enteringAndExitingDetails) {
        JSONObject result = new JSONObject();
        result.put(SPEED_BEFORE_IN_KNOTS, enteringAndExitingDetails.getSpeedWithBearingBefore() == null ? null
                : enteringAndExitingDetails.getSpeedWithBearingBefore().getKnots());
        result.put(COG_BEFORE_IN_TRUE_DEGREES, enteringAndExitingDetails.getSpeedWithBearingBefore() == null ? null
                : enteringAndExitingDetails.getSpeedWithBearingBefore().getBearing().getDegrees());
        result.put(SPEED_AFTER_IN_KNOTS, enteringAndExitingDetails.getSpeedWithBearingAfter() == null ? null
                : enteringAndExitingDetails.getSpeedWithBearingAfter().getKnots());
        result.put(COG_AFTER_IN_TRUE_DEGREES, enteringAndExitingDetails.getSpeedWithBearingAfter() == null ? null
                : enteringAndExitingDetails.getSpeedWithBearingAfter().getBearing().getDegrees());
        result.put(DIRECTION_CHANGE_IN_DEGREES, enteringAndExitingDetails.getDirectionChangeInDegrees());
        return result;
    }

}
