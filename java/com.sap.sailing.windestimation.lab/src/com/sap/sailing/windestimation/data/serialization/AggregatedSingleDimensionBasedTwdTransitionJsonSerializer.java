package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;

public class AggregatedSingleDimensionBasedTwdTransitionJsonSerializer
        implements JsonSerializer<AggregatedSingleDimensionBasedTwdTransition> {

    public static final String DIMENSION_VALUE = "value";
    public static final String MEAN = "mean";
    public static final String STD = "std";

    @Override
    public JSONObject serialize(AggregatedSingleDimensionBasedTwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DIMENSION_VALUE, transition.getDimensionValue());
        json.put(MEAN, transition.getMean());
        json.put(STD, transition.getStd());
        return json;
    }

}
