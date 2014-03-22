package com.sap.sse.datamining.impl.workers.aggregators;

import java.util.Collection;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public abstract class ArithmeticAverageAggregationWorker<ExtractedType, AggregatedType> 
					  extends AbstractAggregationWorker<ExtractedType, AggregatedType>  {
    
    private SumAggregationHelper<ExtractedType, AggregatedType> sumAggregator;

    public ArithmeticAverageAggregationWorker(SumAggregationHelper<ExtractedType, AggregatedType> sumAggregator) {
        this.sumAggregator = sumAggregator;
    }
    
    @Override
    protected AggregatedType aggregate(Collection<ExtractedType> data) {
        return divide(sumAggregator.aggregate(data), data.size());
    }

    protected abstract AggregatedType divide(AggregatedType sum, int dataAmount);

}
