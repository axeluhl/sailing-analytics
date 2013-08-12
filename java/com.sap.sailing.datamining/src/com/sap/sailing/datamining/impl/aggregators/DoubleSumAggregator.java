package com.sap.sailing.datamining.impl.aggregators;

public class DoubleSumAggregator extends SimpleSumAggregator<Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

}
