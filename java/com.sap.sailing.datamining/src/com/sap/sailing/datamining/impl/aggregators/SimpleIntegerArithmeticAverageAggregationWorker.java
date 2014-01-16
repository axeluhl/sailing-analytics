package com.sap.sailing.datamining.impl.aggregators;

import com.sap.sailing.datamining.impl.aggregators.helpers.SimpleIntegerSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.helpers.SumAggregationHelper;

public class SimpleIntegerArithmeticAverageAggregationWorker extends SimpleArithmeticAverageAggregationWorker<Integer> {

    public SimpleIntegerArithmeticAverageAggregationWorker() {
        super((SumAggregationHelper<Integer, Integer>) new SimpleIntegerSumAggregator());
    }

    @Override
    protected Integer divide(Integer sum, int dataAmount) {
        return sum / dataAmount;
    }

}
