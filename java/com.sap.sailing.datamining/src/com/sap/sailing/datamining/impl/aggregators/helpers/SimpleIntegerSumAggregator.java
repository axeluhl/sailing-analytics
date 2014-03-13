package com.sap.sailing.datamining.impl.aggregators.helpers;

public class SimpleIntegerSumAggregator extends SimpleSumAggregator<Integer> {

    @Override
    protected Integer add(Integer value1, Integer value2) {
        return value1 + value2;
    }

}
