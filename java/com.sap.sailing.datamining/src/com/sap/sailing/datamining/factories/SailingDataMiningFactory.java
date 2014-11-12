package com.sap.sailing.datamining.factories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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

    private final ExecutorService executorService;
    
    private final DataMiningStringMessages stringMessages;
    
    private final ProcessorFactory processorFactory;
    
    private final FunctionProvider functionProvider;
    private final DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    public SailingDataMiningFactory(FunctionProvider functionProvider, DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry) {
        DataMiningActivator dataMiningActivator = DataMiningActivator.getDefault();
        executorService = dataMiningActivator.getExecutor();
        stringMessages = dataMiningActivator.getStringMessages();
        
        processorFactory = new ProcessorFactory(executorService);
        
        this.functionProvider = functionProvider;
        this.dataRetrieverChainDefinitionRegistry = dataRetrieverChainDefinitionRegistry;
    }

    public <ElementType> Query<Double> createQuery(RacingEventService dataSource, final QueryDefinition queryDefinition) {
        return new ProcessorQuery<Double, RacingEventService>(executorService, dataSource,
                stringMessages, DataMiningStringMessages.Util.getLocaleFor(queryDefinition.getLocaleInfoName())) {
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
                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(executorService);
                Map<Integer, FilterCriterion<?>> criteriaMappedByDataType = createFilterCriteria(queryDefinition.getFilterSelection());
                do {
                    if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
                        chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrieverLevel()));
                    }
                    
                    chainBuilder.stepDeeper();
                } while (chainBuilder.canStepDeeper());
                if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
                    chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrieverLevel()));
                }
                chainBuilder.addResultReceiver(groupingProcessor);
                
                return chainBuilder.build();
            }
            
        };
    }

    private List<Function<?>> convertDTOsToFunctions(Collection<FunctionDTO> functionDTOs) {
        List<Function<?>> functions = new ArrayList<>();
        for (FunctionDTO functionDTO : functionDTOs) {
            functions.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return functions;
    }
    
    @SuppressWarnings("unchecked")
    private <T> Map<Integer, FilterCriterion<?>> createFilterCriteria(Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection) {
        Map<Integer, CompoundFilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> levelFilterSelection : filterSelection.entrySet()) {
            for (Entry<FunctionDTO, Collection<? extends Serializable>> levelFilterSelectionEntry : levelFilterSelection.getValue().entrySet()) {
                Function<?> function = functionProvider.getFunctionForDTO(levelFilterSelectionEntry.getKey());
                Class<T> dataType = (Class<T>) function.getDeclaringType();
                
                if (!criteriaMappedByRetrieverLevel.containsKey(levelFilterSelection.getKey())) {
                    criteriaMappedByRetrieverLevel.put(levelFilterSelection.getKey(), new AndCompoundFilterCriterion<>(dataType));
                }

                Collection<Object> filterValues = new ArrayList<>(levelFilterSelectionEntry.getValue());
                ((CompoundFilterCriterion<T>) criteriaMappedByRetrieverLevel.get(levelFilterSelection.getKey())).addCriteria(new NullaryFunctionValuesFilterCriterion<>(dataType, function, filterValues));
            }
        }
        return (Map<Integer, FilterCriterion<?>>)(Map<Integer, ?>) criteriaMappedByRetrieverLevel;
    }

    public Query<Set<Object>> createDimensionValuesQuery(RacingEventService dataSource,
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, int retrieverLevel,
            Collection<FunctionDTO> dimensionDTOs, String localeInfoName) {
        final Locale locale = DataMiningStringMessages.Util.getLocaleFor(localeInfoName);
        return new ProcessorQuery<Set<Object>, RacingEventService>(executorService, dataSource, stringMessages, locale) {
            @Override
            protected Processor<RacingEventService, ?> createFirstProcessor() {
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);

                DataRetrieverChainDefinition<RacingEventService> dataRetrieverChainDefinition = dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinition(RacingEventService.class, dataRetrieverChainDefinitionDTO.getId());
                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(executorService);
                Collection<Function<?>> dimensions = convertDTOsToFunctions(dimensionDTOs);
                for (int level = 0; level < retrieverLevel; level++) {
                    chainBuilder.stepDeeper();
                }
                for (Processor<?, ?> resultReceiver : processorFactory.createGroupingExtractorsForDimensions(
                        chainBuilder.getCurrentRetrievedDataType(), valueCollector, dimensions, stringMessages, locale)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                return chainBuilder.build();
            }
        };
    }

}
