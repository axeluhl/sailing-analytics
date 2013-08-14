package com.sap.sailing.datamining.impl.aggregators;

public class SimpleDoubleArithmeticAverageAggregator extends SimpleArithmeticAverageAggregator<Double> {

    public SimpleDoubleArithmeticAverageAggregator() {
        super(new SimpleDoubleSumAggregator());
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
