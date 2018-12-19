package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;

public class AggregatedSingleDimensionBasedTwdTransitionJsonSerializer
        implements JsonSerializer<AggregatedSingleDimensionBasedTwdTransition> {

    public static final String DIMENSION_VALUE = "value";
    public static final String MEAN = "mean";
    public static final String STD = "std";
    public static final String MEDIAN = "median";
    public static final String COUNT = "count";
    public static final String Q1 = "q1";
    public static final String Q3 = "q3";
    public static final String P1 = "p1";
    public static final String P99 = "p99";

    @Override
    public JSONObject serialize(AggregatedSingleDimensionBasedTwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DIMENSION_VALUE, transition.getDimensionValue());
        json.put(MEAN, transition.getMean());
        json.put(STD, transition.getStd());
        json.put(MEDIAN, transition.getMedian());
        json.put(COUNT, transition.getNumberOfValues());
        json.put(Q1, transition.getQ1());
        json.put(Q3, transition.getQ3());
        json.put(P1, transition.getP1());
        json.put(P99, transition.getP99());
        return json;
    }

}
