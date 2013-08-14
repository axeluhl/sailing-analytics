package com.sap.sailing.datamining.impl.aggregators;

import java.util.Collection;

public abstract class ArithmeticAverageAggregator<ExtractedType, AggregatedType> extends AbstractAggregator<ExtractedType, AggregatedType>  {
    
    private SumAggregator<ExtractedType, AggregatedType> sumAggregator;

    public ArithmeticAverageAggregator(SumAggregator<ExtractedType, AggregatedType> sumAggregator) {
        super("Average");
        this.sumAggregator = sumAggregator;
    }

    @Override
    public AggregatedType aggregate(Collection<ExtractedType> data) {
        return divide(sumAggregator.aggregate(data), data.size());
    }

    protected abstract AggregatedType divide(AggregatedType sum, int dataAmount);

}
