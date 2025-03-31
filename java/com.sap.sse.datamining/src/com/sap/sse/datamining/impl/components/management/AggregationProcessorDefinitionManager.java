package com.sap.sse.datamining.impl.components.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.util.ClassUtils;

public class AggregationProcessorDefinitionManager implements AggregationProcessorDefinitionRegistry {
    
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Map<Class<?>, Map<String, AggregationProcessorDefinition<?, ?>>> definitionsMappedByExtractedTypeAndMessageKey;
    
    public AggregationProcessorDefinitionManager() {
        definitionsMappedByExtractedTypeAndMessageKey = new HashMap<>();
    }
    
    @Override
    public Iterable<AggregationProcessorDefinition<?, ?>> getAll() {
        Collection<AggregationProcessorDefinition<?, ?>> allDefinitions = new HashSet<>();
        for (Map<String, AggregationProcessorDefinition<?, ?>> definitionsForType : definitionsMappedByExtractedTypeAndMessageKey.values()) {
            allDefinitions.addAll(definitionsForType.values());
        }
        return allDefinitions;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <ExtractedType> AggregationProcessorDefinition<ExtractedType, ?> get(Class<ExtractedType> extractedType, String aggregationNameMessageKey) {
        AggregationProcessorDefinition<ExtractedType, ?> definition = null;
        Map<String, AggregationProcessorDefinition<?, ?>> definitionsForType = definitionsMappedByExtractedTypeAndMessageKey.get(extractedType);
        if (definitionsForType != null) {
            definition = (AggregationProcessorDefinition<ExtractedType, ?>) definitionsForType
                    .get(aggregationNameMessageKey);
        }
        return definition;
    }

    @Override
    public <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getByExtractedType(Class<ExtractedType> extractedType) {
        Collection<AggregationProcessorDefinition<? super ExtractedType, ?>> definitions = new HashSet<>();
        Class<?> cleanExtractedType = ClassUtils.primitiveTypeToWrapperClass(extractedType);
        Collection<Class<?>> typesToGet = ClassUtils.getSupertypesOf(cleanExtractedType);
        typesToGet.add(cleanExtractedType);
        typesToGet.add(Object.class);
        for (Class<?> type : typesToGet) {
            definitions.addAll(getByUnspecificTypeAsSpecificType(type));
        }
        return definitions;
    }
    
    @SuppressWarnings("unchecked")
    private <ExtractedType> Collection<AggregationProcessorDefinition< ? super ExtractedType, ?>> getByUnspecificTypeAsSpecificType(Class<?> extractedType) {
        Collection<AggregationProcessorDefinition<? super ExtractedType, ?>> definitions = new HashSet<>();
        if (definitionsMappedByExtractedTypeAndMessageKey.containsKey(extractedType)) {
            for (AggregationProcessorDefinition<?, ?> definition : definitionsMappedByExtractedTypeAndMessageKey.get(extractedType).values()) {
                definitions.add((AggregationProcessorDefinition<? super ExtractedType, ?>) definition);
            }
        }
        return definitions;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO,
                                                                                                           ClassLoader classLoader) {
        AggregationProcessorDefinition<ExtractedType, ResultType> aggregatorDefinition = null;
        if (aggregatorDefinitionDTO != null) {
            try {
                Class<ExtractedType> extractedType = (Class<ExtractedType>) ClassUtils.getClassForName(aggregatorDefinitionDTO.getExtractedTypeName(), true, classLoader);
                aggregatorDefinition = (AggregationProcessorDefinition<ExtractedType, ResultType>) get(extractedType, aggregatorDefinitionDTO.getMessageKey());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Couldn't get classes for the aggregator definition DTO " + aggregatorDefinitionDTO, e);
            }
            if (aggregatorDefinition == null) {
                logger.log(Level.WARNING, "No aggregator definition found for the DTO: " + aggregatorDefinitionDTO);
            }
        }
        return aggregatorDefinition;
    }

    @Override
    public boolean register(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        if (get(aggregationProcessorDefinition.getExtractedType(), aggregationProcessorDefinition.getAggregationNameMessageKey()) != null) {
            logger.info("Can't register the aggregation processor definition " + aggregationProcessorDefinition +
                        ", because there's allready a definition registered for the extracted type " +
                        aggregationProcessorDefinition.getExtractedType().getName() + " and the message key " + 
                        aggregationProcessorDefinition.getAggregationNameMessageKey());
            return false;
        }
        
        Class<?> extractedType = aggregationProcessorDefinition.getExtractedType();
        if (!definitionsMappedByExtractedTypeAndMessageKey.containsKey(extractedType)) {
            definitionsMappedByExtractedTypeAndMessageKey.put(extractedType, new HashMap<String, AggregationProcessorDefinition<?,?>>());
        }
        AggregationProcessorDefinition<?, ?> previousDefinition = definitionsMappedByExtractedTypeAndMessageKey.get(extractedType).put(aggregationProcessorDefinition.getAggregationNameMessageKey(), aggregationProcessorDefinition);
        if (previousDefinition == null) {
            logger.info("Registering the aggregation processor definition " + aggregationProcessorDefinition +
                        " for the extracted type " + aggregationProcessorDefinition.getExtractedType().getName() +
                        " and the message key " + aggregationProcessorDefinition.getAggregationNameMessageKey());
        } else {
            logger.info("Registering the aggregation processor definition " + aggregationProcessorDefinition +
                        " for the extracted type " + aggregationProcessorDefinition.getExtractedType().getName() +
                        " and the message key " + aggregationProcessorDefinition.getAggregationNameMessageKey() +
                        " replacing " + previousDefinition);
        }
        return true;
    }

    @Override
    public boolean unregister(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition) {
        boolean changed = false;
        Class<?> extractedType = aggregationProcessorDefinition.getExtractedType();
        if (definitionsMappedByExtractedTypeAndMessageKey.containsKey(extractedType) &&
            definitionsMappedByExtractedTypeAndMessageKey.get(extractedType).remove(aggregationProcessorDefinition.getAggregationNameMessageKey()) != null) {
            changed = true;
        }
        if (changed) {
            logger.info("Unregistering the aggregation processor definition " + aggregationProcessorDefinition +
                        " for the extracted type " + aggregationProcessorDefinition.getExtractedType().getName() +
                        " and the message key " + aggregationProcessorDefinition.getAggregationNameMessageKey());
        } else {
            logger.info("Can't unregister the aggregation processor definition " + aggregationProcessorDefinition +
                        ", because there's no definition registered for the extracted type " +
                        aggregationProcessorDefinition.getExtractedType().getName() + " and the message key " + 
                        aggregationProcessorDefinition.getAggregationNameMessageKey());
        }
        return changed;
    }

}
