package com.sap.sailing.datamining.impl.aggregators;

public abstract class DoubleSumAggregator<ExtractedType> extends SumAggregator<ExtractedType, Double> {

    @Override
    protected Double add(Double value1, Double value2) {
        return value1 + value2;
    }

}
