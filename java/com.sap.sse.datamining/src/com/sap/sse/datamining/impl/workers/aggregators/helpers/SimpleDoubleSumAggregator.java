package com.sap.sse.datamining.impl.workers.aggregators.helpers;

import com.sap.sse.datamining.impl.workers.aggregators.SimpleSumAggregator;

public class SimpleDoubleSumAggregator extends SimpleSumAggregator<Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

}
