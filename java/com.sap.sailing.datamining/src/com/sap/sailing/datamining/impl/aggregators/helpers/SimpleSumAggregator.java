package com.sap.sailing.datamining.impl.aggregators.helpers;

public abstract class SimpleSumAggregator<T> extends SumAggregationHelper<T, T> {

    @Override
    protected T getValueFor(T extractedValue) {
        return extractedValue;
    }

}
