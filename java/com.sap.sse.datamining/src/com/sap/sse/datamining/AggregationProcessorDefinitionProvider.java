package com.sap.sse.datamining;

public interface AggregationProcessorDefinitionProvider {
    
    public <ExtractedType> Iterable<AggregationProcessorDefinition<ExtractedType, ?>> get(Class<ExtractedType> extractedType);

}
