package com.sap.sailing.datamining.impl.aggregators;

public abstract class IntegerSumAggregator<ExtractedType> extends SumAggregator<ExtractedType, Integer> {

    @Override
    protected Integer add(Integer value1, Integer value2) {
        return value1 + value2;
    }

}
