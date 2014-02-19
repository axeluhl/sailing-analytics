package com.sap.sse.datamining.impl.workers.aggregators;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleIntegerSumAggregator;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public class SimpleIntegerArithmeticAverageAggregationWorker extends SimpleArithmeticAverageAggregationWorker<Integer> {

    public SimpleIntegerArithmeticAverageAggregationWorker() {
        super((SumAggregationHelper<Integer, Integer>) new SimpleIntegerSumAggregator());
    }

    @Override
    protected Integer divide(Integer sum, int dataAmount) {
        return sum / dataAmount;
    }

}
