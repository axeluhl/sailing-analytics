package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.Aggregator;

public abstract class AverageAggregator<ExtractedType, AggregatedType> implements Aggregator<ExtractedType, AggregatedType>  {

    @Override
    public AggregatedType aggregate(Collection<ExtractedType> data) {
        SumAggregator<ExtractedType, AggregatedType> sumAggregator = new SumAggregator<ExtractedType, AggregatedType>() {
            @Override
            protected AggregatedType add(AggregatedType value1, AggregatedType value2) {
                return AverageAggregator.this.add(value1, value2);
            }
            @Override
            protected AggregatedType getValueFor(ExtractedType extractedValue) {
                return AverageAggregator.this.getValueFor(extractedValue);
            }
        };
        return divide(sumAggregator.aggregate(data), data.size());
    }

    protected abstract AggregatedType add(AggregatedType value1, AggregatedType value2);
    protected abstract AggregatedType getValueFor(ExtractedType extractedValue);
    protected abstract AggregatedType divide(AggregatedType sum, int dataAmount);

}
