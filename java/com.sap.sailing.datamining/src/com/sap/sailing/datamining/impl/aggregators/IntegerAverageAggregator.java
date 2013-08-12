package com.sap.sailing.datamining.impl.aggregators;

public class IntegerAverageAggregator extends SimpleAverageAggregator<Integer> {

    public IntegerAverageAggregator() {
        super(new IntegerSumAggregator());
    }

    @Override
    protected Integer divide(Integer sum, int dataAmount) {
        return sum / dataAmount;
    }

}
