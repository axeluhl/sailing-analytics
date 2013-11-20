package com.sap.sailing.datamining.impl.aggregators.helpers;

public class SimpleDoubleSumAggregator extends SimpleSumAggregator<Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

}
