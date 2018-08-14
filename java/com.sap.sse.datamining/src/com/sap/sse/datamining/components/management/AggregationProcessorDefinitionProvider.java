package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;

public interface AggregationProcessorDefinitionProvider {
    
    public Iterable<AggregationProcessorDefinition<?, ?>> getAll();
    public <ExtractedType> AggregationProcessorDefinition<ExtractedType, ?> get(Class<ExtractedType> extractedType, String aggregationNameMessageKey);
    public <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getByExtractedType(Class<ExtractedType> extractedType);
    public <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO, ClassLoader classLoader);

}
