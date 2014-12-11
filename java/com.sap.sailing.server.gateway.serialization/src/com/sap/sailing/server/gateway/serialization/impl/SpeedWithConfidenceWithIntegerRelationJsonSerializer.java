package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SpeedWithConfidenceWithIntegerRelationJsonSerializer implements
        JsonSerializer<SpeedWithConfidence<Integer>> {

    public static final String FIELD_SPEED = "speed";
    public static final String FIELD_CONFIDENCE = "confidence";
    public static final String FIELD_RELATION_INT = "relationInt";

    @Override
    public JSONObject serialize(SpeedWithConfidence<Integer> speed) {
        JSONObject object = new JSONObject();
        object.put(FIELD_SPEED, speed.getObject().getKnots());
        object.put(FIELD_CONFIDENCE, speed.getConfidence());
        object.put(FIELD_RELATION_INT, speed.getRelativeTo());
        return object;
    }

}
