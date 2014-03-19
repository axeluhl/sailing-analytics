package com.sap.sse.datamining.impl.workers.aggregators.helpers;


public class SimpleDoubleSumAggregationHelper extends SimpleSumAggregationHelper<Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

}
