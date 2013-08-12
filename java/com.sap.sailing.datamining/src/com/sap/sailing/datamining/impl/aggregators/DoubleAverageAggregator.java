package com.sap.sailing.datamining.impl.aggregators;

public class DoubleAverageAggregator extends SimpleAverageAggregator<Double> {

    public DoubleAverageAggregator() {
        super(new DoubleSumAggregator());
    }

    @Override
    protected Double divide(Double sum, int dataAmount) {
        return sum / dataAmount;
    }

}
