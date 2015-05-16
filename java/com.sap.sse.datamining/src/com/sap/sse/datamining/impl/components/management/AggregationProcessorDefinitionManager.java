package com.sap.sse.datamining.impl.components.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;
import com.sap.sse.datamining.util.Classes;

public class AggregationProcessorDefinitionManager implements AggregationProcessorDefinitionRegistry {
    
    private final Map<Class<?>, Map<String, AggregationProcessorDefinition<?, ?>>> definitionsMappedByExtractedType;
    
    public AggregationProcessorDefinitionManager() {
        definitionsMappedByExtractedType = new HashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <ExtractedType> AggregationProcessorDefinition<ExtractedType, ?> get(Class<ExtractedType> extractedType,
            String aggregationNameMessageKey) {
        if (definitionsMappedByExtractedType.containsKey(extractedType)) {
            return (AggregationProcessorDefinition<ExtractedType, ?>) definitionsMappedByExtractedType.get(extractedType).get(aggregationNameMessageKey);
        }
        return null;
    }

    @Override
    public <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getByExtractedType(
            Class<ExtractedType> extractedType) {
        Collection<AggregationProcessorDefinition<? super ExtractedType, ?>> definitions = new HashSet<>();
        Collection<Class<?>> typesToGet = Classes.getSupertypesOf(extractedType);
        typesToGet.add(extractedType);
        typesToGet.add(Object.class);
        for (Class<?> type : typesToGet) {
            definitions.addAll(getByUnspecificTypeAsSpecificType(type));
        }
        return definitions;
    }
    
    @SuppressWarnings("unchecked")
    private <ExtractedType> Collection<AggregationProcessorDefinition< ? super ExtractedType, ?>> getByUnspecificTypeAsSpecificType(Class<?> extractedType) {
        Collection<AggregationProcessorDefinition<? super ExtractedType, ?>> definitions = new HashSet<>();
        if (definitionsMappedByExtractedType.containsKey(extractedType)) {
            for (AggregationProcessorDefinition<?, ?> definition : definitionsMappedByExtractedType.get(extractedType).values()) {
                definitions.add((AggregationProcessorDefinition<? super ExtractedType, ?>) definition);
            }
        }
        return definitions;
    }

    @Override
    public boolean register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        if (get(aggregationProcessorDefinition.getExtractedType(), aggregationProcessorDefinition.getAggregationNameMessageKey()) != null) {
            return false;
        }
        
        Class<?> extractedType = aggregationProcessorDefinition.getExtractedType();
        if (!definitionsMappedByExtractedType.containsKey(extractedType)) {
            definitionsMappedByExtractedType.put(extractedType, new HashMap<String, AggregationProcessorDefinition<?,?>>());
        }
        definitionsMappedByExtractedType.get(extractedType).put(aggregationProcessorDefinition.getAggregationNameMessageKey(), aggregationProcessorDefinition);
        return true;
    }

    @Override
    public boolean unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        Class<?> extractedType = aggregationProcessorDefinition.getExtractedType();
        if (!definitionsMappedByExtractedType.containsKey(extractedType)) {
            return false;
        }
        return definitionsMappedByExtractedType.get(extractedType).remove(aggregationProcessorDefinition.getAggregationNameMessageKey()) != null;
    }

}
