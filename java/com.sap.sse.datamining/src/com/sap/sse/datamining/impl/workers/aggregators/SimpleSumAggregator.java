package com.sap.sse.datamining.impl.workers.aggregators;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public abstract class SimpleSumAggregator<T> extends SumAggregationHelper<T, T> {

    @Override
    protected T getValueFor(T extractedValue) {
        return extractedValue;
    }

}
