package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class AggregatedSingleDimensionBasedTwdTransitionJsonDeserializer
        implements JsonDeserializer<AggregatedSingleDimensionBasedTwdTransition> {

    @Override
    public AggregatedSingleDimensionBasedTwdTransition deserialize(JSONObject object)
            throws JsonDeserializationException {
        double dimensionValue = (double) object
                .get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.DIMENSION_VALUE);
        double mean = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.MEAN);
        double std = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.STD);
        double zeroMeanStd = (double) object
                .get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.ZERO_MEAN_STD);
        double median = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.MEDIAN);
        long count = (long) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.COUNT);
        double q1 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.Q1);
        double q3 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.Q3);
        double p1 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.P1);
        double p99 = (double) object.get(AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.P99);
        AggregatedSingleDimensionBasedTwdTransition twdTransition = new AggregatedSingleDimensionBasedTwdTransition(
                dimensionValue, mean, std, zeroMeanStd, median, count, q1, q3, p1, p99);
        return twdTransition;
    }

}
