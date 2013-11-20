package com.sap.sailing.datamining.impl.aggregators;

public abstract class SimpleMedianAggregationWorker<T extends Comparable<? super T>> extends MedianAggregationWorker<T, T> {

    @Override
    protected T getAggregatedValueFor(T extractedType) {
        return extractedType;
    }

}