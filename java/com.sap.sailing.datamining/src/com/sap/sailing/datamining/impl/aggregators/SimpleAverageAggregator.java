package com.sap.sailing.datamining.impl.aggregators;


public abstract class SimpleAverageAggregator<T> extends AverageAggregator<T, T> {

    public SimpleAverageAggregator(SumAggregator<T, T> sumAggregator) {
        super(sumAggregator);
    }

}
