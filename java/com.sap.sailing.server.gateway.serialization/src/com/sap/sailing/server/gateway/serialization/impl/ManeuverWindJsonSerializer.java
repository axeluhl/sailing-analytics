package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWindJsonSerializer implements JsonSerializer<Wind> {

    public static final String DIRECTION_IN_TRUE_DEGREES = "directionInTrueDegrees";
    public static final String SPEED_IN_KNOTS = "speedInKnots";

    @Override
    public JSONObject serialize(Wind wind) {
        JSONObject result = new JSONObject();
        result.put(DIRECTION_IN_TRUE_DEGREES, wind.getBearing().getDegrees());
        result.put(SPEED_IN_KNOTS, wind.getKnots());
        return result;
    }

}
