package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;

public interface AggregationProcessorDefinitionProvider {
    
    public <ExtractedType> AggregationProcessorDefinition<ExtractedType, ?> get(Class<ExtractedType> extractedType, String aggregationNameMessageKey);
    public <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getByExtractedType(Class<ExtractedType> extractedType);

}
