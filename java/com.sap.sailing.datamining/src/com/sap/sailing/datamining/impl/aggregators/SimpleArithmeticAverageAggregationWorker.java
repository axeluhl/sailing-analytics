package com.sap.sailing.datamining.impl.aggregators;

import com.sap.sailing.datamining.impl.aggregators.helpers.SumAggregationHelper;


public abstract class SimpleArithmeticAverageAggregationWorker<T> extends ArithmeticAverageAggregationWorker<T, T> {

    public SimpleArithmeticAverageAggregationWorker(SumAggregationHelper<T, T> sumAggregator) {
        super(sumAggregator);
    }

}
