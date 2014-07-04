package com.sap.sse.datamining.impl.workers.aggregators.helpers;


public class SimpleIntegerSumAggregationHelper extends SimpleSumAggregationHelper<Integer> {

    @Override
    protected Integer add(Integer value1, Integer value2) {
        return value1 + value2;
    }

}
