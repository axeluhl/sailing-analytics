package com.sap.sse.datamining.impl.workers.aggregators.helpers;

import com.sap.sse.datamining.impl.workers.aggregators.SimpleSumAggregator;

public class SimpleIntegerSumAggregator extends SimpleSumAggregator<Integer> {

    @Override
    protected Integer add(Integer value1, Integer value2) {
        return value1 + value2;
    }

}
