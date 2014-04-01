package com.sap.sse.datamining.impl.workers.aggregators;

public class SimpleDoubleMedianAggregationWorker extends SimpleMedianAggregationWorker<Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

    @Override
    protected Double divideByTwo(Double value) {
        return value / 2.0;
    }

}
