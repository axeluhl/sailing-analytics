package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

public interface AggregationProcessorDefinition<ExtractedType, AggregatedType> {
    
    public Class<ExtractedType> getExtractedType();
    public Class<AggregatedType> getAggregatedType();
    
    public String getAggregationNameMessageKey();
    
    public Class<? extends AbstractParallelGroupedDataStoringAggregationProcessor<ExtractedType, AggregatedType>> getAggregationProcessor();
    public Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, AggregatedType>> construct(ExecutorService executor, Collection<Processor<Map<GroupKey, AggregatedType>, ?>> resultReceivers);
    
}
