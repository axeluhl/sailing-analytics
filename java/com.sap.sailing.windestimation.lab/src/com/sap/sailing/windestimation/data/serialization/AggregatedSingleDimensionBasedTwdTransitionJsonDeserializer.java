package com.sap.sailing.windestimation.data.serialization;

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
        double median = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.MEDIAN);
        long count = (long) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.COUNT);
        double q1 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.Q1);
        double q3 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.Q3);
        double p1 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.P1);
        double p99 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.P99);
        AggregatedSingleDimensionBasedTwdTransition twdTransition = new AggregatedSingleDimensionBasedTwdTransition(
                dimensionValue, mean, std, median, count, q1, q3, p1, p99);
        return twdTransition;
    }

}
