package com.sap.sse.datamining;

import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;

public interface AggregationProcessorDefinition<ExtractedType, AggregatedType> {
    
    public Class<ExtractedType> getExtractedType();
    public Class<AggregatedType> getAggregatedType();
    
    public String getAggregationNameMessageKey();
    
    public Class<AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> getAggregationProcessor();

}
