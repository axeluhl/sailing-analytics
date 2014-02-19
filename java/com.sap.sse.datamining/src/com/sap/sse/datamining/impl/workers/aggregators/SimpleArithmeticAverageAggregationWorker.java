package com.sap.sse.datamining.impl.workers.aggregators;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public abstract class SimpleArithmeticAverageAggregationWorker<T> extends ArithmeticAverageAggregationWorker<T, T> {

    public SimpleArithmeticAverageAggregationWorker(SumAggregationHelper<T, T> sumAggregator) {
        super(sumAggregator);
    }

}
