package com.sap.sse.datamining.impl.workers.aggregators;

import java.util.Collection;

import com.sap.sse.datamining.impl.workers.aggregators.helpers.SumAggregationHelper;

public class SumAggregationWorker<ExtractedType, AggregatedType>
			 extends AbstractAggregationWorker<ExtractedType, AggregatedType> {
    
    private SumAggregationHelper<ExtractedType, AggregatedType> aggregator;
    
    public SumAggregationWorker(SumAggregationHelper<ExtractedType, AggregatedType> aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    protected AggregatedType aggregate(Collection<ExtractedType> data) {
        return aggregator.aggregate(data);
    }

}
