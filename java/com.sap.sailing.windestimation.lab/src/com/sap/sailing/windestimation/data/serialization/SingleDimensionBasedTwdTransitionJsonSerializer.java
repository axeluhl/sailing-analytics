package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;

public class SingleDimensionBasedTwdTransitionJsonSerializer
        implements JsonSerializer<SingleDimensionBasedTwdTransition> {

    public static final String DIMENSION_VALUE = "value";
    public static final String TWD_CHANGE = "twdChange";

    @Override
    public JSONObject serialize(SingleDimensionBasedTwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DIMENSION_VALUE, transition.getDimensionValue());
        json.put(TWD_CHANGE, transition.getAbsTwdChangeInDegrees());
        return json;
    }

}
