package com.sap.sailing.datamining.impl.aggregators;

public abstract class SimpleSumAggregator<T> extends SumAggregator<T, T> {

    @Override
    protected T getValueFor(T extractedValue) {
        return extractedValue;
    }

}
