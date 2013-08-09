package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.Aggregator;

public abstract class AverageAggregator<ExtractedType> implements Aggregator<ExtractedType, Double>  {

    @Override
    public Double aggregate(Collection<ExtractedType> data) {
        SumAggregator<ExtractedType> sumAggregator = new SumAggregator<ExtractedType>() {
            @Override
            protected Number add(Number number1, Number number2) {
                return AverageAggregator.this.add(number1, number2);
            }
            @Override
            protected Number getNumericValue(ExtractedType value) {
                return AverageAggregator.this.getNumericValue(value);
            }
        };
        return sumAggregator.aggregate(data).doubleValue() / data.size();
    }

    protected abstract Number add(Number number1, Number number2);
    protected abstract Number getNumericValue(ExtractedType value);

}
