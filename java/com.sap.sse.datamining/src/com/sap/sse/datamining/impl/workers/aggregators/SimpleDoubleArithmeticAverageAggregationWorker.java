package com.sap.sse.datamining.impl.workers.aggregators;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SimpleDoubleSumAggregator;
import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public class SimpleDoubleArithmeticAverageAggregationWorker extends SimpleArithmeticAverageAggregationWorker<Double> {

    public SimpleDoubleArithmeticAverageAggregationWorker() {
        super((SumAggregationHelper<Double, Double>) new SimpleDoubleSumAggregator());
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
