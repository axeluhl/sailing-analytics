package com.sap.sse.datamining.impl.workers.aggregators.helpers;


public abstract class SimpleSumAggregationHelper<T> extends SumAggregationHelper<T, T> {

    @Override
    protected T getValueFor(T extractedValue) {
        return extractedValue;
    }

}
