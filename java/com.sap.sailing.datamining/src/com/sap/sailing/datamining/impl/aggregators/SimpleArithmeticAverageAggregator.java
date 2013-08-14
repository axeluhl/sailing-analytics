package com.sap.sailing.datamining.impl.aggregators;


public abstract class SimpleArithmeticAverageAggregator<T> extends ArithmeticAverageAggregator<T, T> {

    public SimpleArithmeticAverageAggregator(SumAggregator<T, T> sumAggregator) {
        super(sumAggregator);
    }

}
