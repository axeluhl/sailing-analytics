package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;

public interface AggregationProcessorDefinitionProvider {
    
    Iterable<AggregationProcessorDefinition<?, ?>> getAll();
    <ExtractedType> AggregationProcessorDefinition<ExtractedType, ?> get(Class<ExtractedType> extractedType, String aggregationNameMessageKey);
    <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getByExtractedType(Class<ExtractedType> extractedType);
    <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO, ClassLoader classLoader);

}
