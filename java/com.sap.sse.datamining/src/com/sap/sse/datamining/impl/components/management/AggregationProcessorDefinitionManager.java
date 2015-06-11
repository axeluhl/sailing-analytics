package com.sap.sse.datamining.impl.components.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;
import com.sap.sse.datamining.factories.DataMiningDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.criterias.FunctionMatchesDTOFilterCriterion;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.util.Classes;

public class AggregationProcessorDefinitionManager implements AggregationProcessorDefinitionRegistry {
    
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DataMiningDTOFactory dtoFactory;
    
    private final Map<Class<?>, Map<String, AggregationProcessorDefinition<?, ?>>> definitionsMappedByExtractedType;
    
    public AggregationProcessorDefinitionManager() {
        dtoFactory = new DataMiningDTOFactory();
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
    public <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO) {
        if (aggregatorDefinitionDTO == null) {
            return null;
        }
        
        Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> definitionsMatchingDTO = getDefinitionsForDTO(aggregatorDefinitionDTO);
        if (moreThanOneDefinitionMatchedDTO(definitionsMatchingDTO)) {
            logThatMoreThanOneDefinitionMatchedDTO(aggregatorDefinitionDTO, definitionsMatchingDTO);
        }
        
        AggregationProcessorDefinition<ExtractedType, ResultType> aggregatorDefinition = getDefinitionToReturn(definitionsMatchingDTO);
        if (aggregatorDefinition == null) {
            logger.log(Level.WARNING, "No aggregator definition found for the DTO: " + aggregatorDefinitionDTO);
        }
        return aggregatorDefinition;
    }

    @SuppressWarnings("unchecked")
    private <ExtractedType, ResultType> Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> getDefinitionsForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO) {
        Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> definitionsDTO = new HashSet<>();
        for (AggregationProcessorDefinition<?, ?> definition : getAllDefinitions()) {
            AggregationProcessorDefinitionDTO definitionDTO = dtoFactory.createAggregationProcessorDefinitionDTO(definition);
            if (aggregatorDefinitionDTO.equals(definitionDTO)) {
                definitionsDTO.add((AggregationProcessorDefinition<ExtractedType, ResultType>) definition);
            }
        }
        return definitionsDTO;
    }

    private Collection<AggregationProcessorDefinition<?, ?>> getAllDefinitions() {
        Collection<AggregationProcessorDefinition<?, ?>> allDefinitions = new HashSet<>();
        for (Map<String, AggregationProcessorDefinition<?, ?>> map : definitionsMappedByExtractedType.values()) {
            allDefinitions.addAll(map.values());
        }
        return allDefinitions;
    }

    private <ExtractedType, ResultType> boolean moreThanOneDefinitionMatchedDTO(Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> definitionsMatchingDTO) {
        return definitionsMatchingDTO.size() > 1;
    }

    private <ExtractedType, ResultType> void logThatMoreThanOneDefinitionMatchedDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO,
                                                                                    Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> definitionsMatchingDTO) {
        logger.log(Level.FINER, "More than on registered aggregator definition matched the DTO '" + aggregatorDefinitionDTO + "'");
        for (AggregationProcessorDefinition<ExtractedType, ResultType> definition : definitionsMatchingDTO) {
            logger.log(Level.FINEST, "The definition '" + definition + "' matched the DTO '" + aggregatorDefinitionDTO + "'");
        }
    }

    private <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getDefinitionToReturn(Collection<AggregationProcessorDefinition<ExtractedType, ResultType>> definitionsMatchingDTO) {
        return definitionsMatchingDTO.isEmpty() ? null : definitionsMatchingDTO.iterator().next();
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
