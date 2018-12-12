package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;

public class AggregatedSingleDimensionBasedTwdTransitionJsonDeserializer
        implements JsonDeserializer<AggregatedSingleDimensionBasedTwdTransition> {

    @Override
    public AggregatedSingleDimensionBasedTwdTransition deserialize(JSONObject object)
            throws JsonDeserializationException {
        double dimensionValue = (double) object
                .get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.DIMENSION_VALUE);
        double mean = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.MEAN);
        double std = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.STD);
        AggregatedSingleDimensionBasedTwdTransition twdTransition = new AggregatedSingleDimensionBasedTwdTransition(
                dimensionValue, mean, std);
        return twdTransition;
    }

}
