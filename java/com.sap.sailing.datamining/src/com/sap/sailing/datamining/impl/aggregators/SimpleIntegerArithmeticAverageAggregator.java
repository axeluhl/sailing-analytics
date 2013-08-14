package com.sap.sailing.datamining.impl.aggregators;

public class SimpleIntegerArithmeticAverageAggregator extends SimpleArithmeticAverageAggregator<Integer> {

    public SimpleIntegerArithmeticAverageAggregator() {
        super(new SimpleIntegerSumAggregator());
    }

    @Override
    protected Integer divide(Integer sum, int dataAmount) {
        return sum / dataAmount;
    }

}
