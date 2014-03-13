package com.sap.sailing.datamining.impl.aggregators;

public class SimpleIntegerMedianAggregationWorker extends SimpleMedianAggregationWorker<Integer> {

    @Override
    protected Integer add(Integer value1, Integer value2) {
        return value1 + value2;
    }

    @Override
    protected Integer divideByTwo(Integer value) {
        return value / 2;
    }

}
