package com.sap.sailing.datamining.impl.aggregators;

import java.util.Collection;

import com.sap.sailing.datamining.impl.aggregators.helpers.SumAggregationHelper;

public abstract class ArithmeticAverageAggregationWorker<ExtractedType, AggregatedType> extends AbstractAggregationWorker<ExtractedType, AggregatedType>  {
    
    private SumAggregationHelper<ExtractedType, AggregatedType> sumAggregator;

    public ArithmeticAverageAggregationWorker(SumAggregationHelper<ExtractedType, AggregatedType> sumAggregator) {
        this.sumAggregator = sumAggregator;
    }
    
    @Override
    protected AggregatedType aggregate(Collection<ExtractedType> data) {
        return divide(sumAggregator.aggregate(data), data.size());
    }

//    private AggregatedType aggregate(Collection<ExtractedType> data) {
//        return divide(sumAggregator.aggregate(data), data.size());
//    }

    protected abstract AggregatedType divide(AggregatedType sum, int dataAmount);

}
