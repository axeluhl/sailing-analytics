package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;

public interface AggregationProcessorDefinitionProvider {
    
    public <ExtractedType> Iterable<AggregationProcessorDefinition<ExtractedType, ?>> get(Class<ExtractedType> extractedType);

}
