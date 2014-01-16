package com.sap.sailing.datamining.impl.aggregators;

import com.sap.sailing.datamining.impl.aggregators.helpers.SimpleDoubleSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.helpers.SumAggregationHelper;

public class SimpleDoubleArithmeticAverageAggregationWorker extends SimpleArithmeticAverageAggregationWorker<Double> {

    public SimpleDoubleArithmeticAverageAggregationWorker() {
        super((SumAggregationHelper<Double, Double>) new SimpleDoubleSumAggregator());
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
