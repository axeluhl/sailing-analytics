package com.sap.sailing.datamining.factories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.DataRetrieverChainDefinitionRegistry;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NullaryFunctionValuesFilterCriterion;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public class SailingDataMiningFactory {

    private final ProcessorFactory processorFactory;
    
    private final FunctionProvider functionProvider;
    private final DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    public SailingDataMiningFactory(FunctionProvider functionProvider, DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry) {
        processorFactory = new ProcessorFactory(DataMiningActivator.getExecutor());
        
        this.functionProvider = functionProvider;
        this.dataRetrieverChainDefinitionRegistry = dataRetrieverChainDefinitionRegistry;
    }

    public <ElementType> Query<Double> createQuery(RacingEventService dataSource, final QueryDefinition queryDefinition) {
        return new ProcessorQuery<Double, RacingEventService>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFor(queryDefinition.getLocaleInfoName())) {
            @Override
            protected Processor<RacingEventService, ?> createFirstProcessor() {
                @SuppressWarnings("unchecked") // TODO Clean, after the deprecated components have been removed
                Function<Double> extractionFunction = (Function<Double>) functionProvider.getFunctionForDTO(queryDefinition.getStatisticToCalculate());
                @SuppressWarnings("unchecked")
                Class<ElementType> dataTypeToRetrieve = (Class<ElementType>) extractionFunction.getDeclaringType();
                
                Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> aggregationProcessor = processorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType());
                Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<Double>> extractionProcessor = processorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                List<Function<?>> dimensionsToGroupBy = convertDTOsToFunctions(queryDefinition.getDimensionsToGroupBy());
                Processor<ElementType, GroupedDataEntry<ElementType>> groupingProcessor = processorFactory.createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, dimensionsToGroupBy);

                DataRetrieverChainDefinition<RacingEventService> dataRetrieverChainDefinition = dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinition(RacingEventService.class, queryDefinition.getDataRetrieverChainDefinition().getId());
                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(DataMiningActivator.getExecutor());
                Map<Class<?>, FilterCriterion<?>> criteriaMappedByDataType = createFilterCriteria(queryDefinition.getFilterSelection());
                do {
                    if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrievedDataType())) {
                        chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrievedDataType()));
                    }
                    
                    chainBuilder.stepDeeper();
                } while (!dataTypeToRetrieve.equals(chainBuilder.getCurrentRetrievedDataType()));
                if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrievedDataType())) {
                    chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrievedDataType()));
                }
                chainBuilder.addResultReceiver(groupingProcessor);
                
                return chainBuilder.build();
            }
            
        };
    }

    private List<Function<?>> convertDTOsToFunctions(Collection<FunctionDTO> functionDTOs) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : functionDTOs) {
            dimensionsToGroupBy.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
    @SuppressWarnings("unchecked")
    private <T> Map<Class<?>, FilterCriterion<?>> createFilterCriteria(Map<FunctionDTO, Iterable<? extends Serializable>> filterSelection) {
        Map<Class<?>, CompoundFilterCriterion<?>> criteriaMappedByDataType = new HashMap<>();
        for (Entry<FunctionDTO, Iterable<? extends Serializable>> filterSelectionEntry : filterSelection.entrySet()) {
            Function<?> function = functionProvider.getFunctionForDTO(filterSelectionEntry.getKey());
            Class<T> dataType = (Class<T>) function.getDeclaringType();
            
            if (!criteriaMappedByDataType.containsKey(dataType)) {
                criteriaMappedByDataType.put(dataType, new AndCompoundFilterCriterion<>(dataType));
            }

            Collection<Object> filterValues = new ArrayList<>();
            for (Object filterValue : filterSelectionEntry.getValue()) {
                filterValues.add(filterValue);
            }
            ((CompoundFilterCriterion<T>) criteriaMappedByDataType.get(dataType)).addCriteria(new NullaryFunctionValuesFilterCriterion<>(dataType, function, filterValues));
        }
        return (Map<Class<?>, FilterCriterion<?>>)(Map<Class<?>, ?>) criteriaMappedByDataType;
    }

    public Query<Set<Object>> createDimensionValuesQuery(RacingEventService dataSource, final DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, final String localeInfoName) {
        final DataMiningStringMessages stringMessages = DataMiningActivator.getStringMessages();
        final Locale locale = DataMiningStringMessages.Util.getLocaleFor(localeInfoName);
        return new ProcessorQuery<Set<Object>, RacingEventService>(DataMiningActivator.getExecutor(), dataSource, stringMessages, locale) {
            @Override
            protected Processor<RacingEventService, ?> createFirstProcessor() {
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);

                DataRetrieverChainDefinition<RacingEventService> dataRetrieverChainDefinition = dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinition(RacingEventService.class, dataRetrieverChainDefinitionDTO.getId());
                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(DataMiningActivator.getExecutor());
                Collection<Function<?>> dimensions = functionProvider.getMinimizedDimensionsFor(dataRetrieverChainDefinition);
                Map<Class<?>, Collection<Function<?>>> dimensionsMappedByDeclaringType = mapFunctionsByDeclaringType(dimensions);
                while (!dimensionsMappedByDeclaringType.isEmpty()) {
                    Class<?> dataType = chainBuilder.getCurrentRetrievedDataType();

                    if (dimensionsMappedByDeclaringType.containsKey(dataType)) {
                        for (Processor<?, ?> resultReceiver : processorFactory.createGroupingExtractorsForDimensions(
                                                                dataType, valueCollector, dimensionsMappedByDeclaringType.get(dataType),
                                                                stringMessages, locale)) {
                            chainBuilder.addResultReceiver(resultReceiver);
                        }
                        dimensionsMappedByDeclaringType.remove(dataType);
                    }
                    
                    if (!dimensionsMappedByDeclaringType.isEmpty()) {
                        chainBuilder.stepDeeper();
                    }
                }
                
                return chainBuilder.build();
            }
        };
    }

    private Map<Class<?>, Collection<Function<?>>> mapFunctionsByDeclaringType(Collection<Function<?>> functions) {
        Map<Class<?>, Collection<Function<?>>> mappedFunctions = new HashMap<>();
        for (Function<?> function : functions) {
            Class<?> declaringType = function.getDeclaringType();
            if (!mappedFunctions.containsKey(declaringType)) {
                mappedFunctions.put(declaringType, new HashSet<Function<?>>());
            }
            mappedFunctions.get(declaringType).add(function);
        }
        return mappedFunctions;
    }

}
