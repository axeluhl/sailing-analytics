package com.sap.sailing.datamining.impl.aggregators;

public abstract class SimpleMedianAggregator<T extends Comparable<? super T>> extends MedianAggregator<T, T> {

    @Override
    protected T getAggregatedValueFor(T extractedType) {
        return extractedType;
    }

}