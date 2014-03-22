package com.sap.sse.datamining.impl.workers.aggregators;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleDoubleSumAggregationHelper;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public class SimpleDoubleArithmeticAverageAggregationWorker extends SimpleArithmeticAverageAggregationWorker<Double> {

    public SimpleDoubleArithmeticAverageAggregationWorker() {
        super((SumAggregationHelper<Double, Double>) new SimpleDoubleSumAggregationHelper());
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
