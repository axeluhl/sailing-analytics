package com.sap.sse.datamining.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NullaryFunctionValuesFilterCriterion;
import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class DataMiningServerImpl implements ModifiableDataMiningServer {
    
    private final CompoundDataMiningStringMessages stringMessages;
    private final ExecutorService executorService;
    
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;
    
    private DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    public DataMiningServerImpl(ExecutorService executorService, FunctionRegistry functionRegistry, FunctionProvider functionProvider, DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry) {
        this.stringMessages = new CompoundDataMiningStringMessages();
        this.executorService = executorService;
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
        this.dataRetrieverChainDefinitionRegistry = dataRetrieverChainDefinitionRegistry;
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    @Override
    public DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }

    @Override
    public void addStringMessages(DataMiningStringMessages stringMessages) {
        this.stringMessages.addStringMessages(stringMessages);
    }

    @Override
    public void removeStringMessages(DataMiningStringMessages stringMessages) {
        this.stringMessages.removeStringMessages(stringMessages);
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    @Override
    public void registerAllWithInternalFunctionPolicy(Iterable<Class<?>> classesToScan) {
        functionRegistry.registerAllWithInternalFunctionPolicy(classesToScan);
    }
    
    @Override
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan) {
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Override
    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister) {
        functionRegistry.unregisterAllFunctionsOf(classesToUnregister);
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        return functionProvider;
    }

    @Override
    public Iterable<Function<?>> getAllStatistics() {
        return functionProvider.getAllStatistics();
    }

    @Override
    public Iterable<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return functionProvider.getFunctionsFor(sourceType);
    }

    @Override
    public Iterable<Function<?>> getStatisticsFor(Class<?> sourceType) {
        return functionProvider.getStatisticsFor(sourceType);
    }

    @Override
    public Iterable<Function<?>> getDimensionsFor(Class<?> sourceType) {
        return functionProvider.getDimensionsFor(sourceType);
    }

    @Override
    public Iterable<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        return functionProvider.getDimensionsFor(dataRetrieverChainDefinition);
    }

    @Override
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO) {
        return functionProvider.getFunctionForDTO(functionDTO);
    }
    
    @Override
    public DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider() {
        return dataRetrieverChainDefinitionRegistry;
    }
    
    @Override
    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        dataRetrieverChainDefinitionRegistry.register(dataRetrieverChainDefinition);
    }
    
    @Override
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        dataRetrieverChainDefinitionRegistry.unregister(dataRetrieverChainDefinition);
    }
    
    @Override
    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType) {
        return dataRetrieverChainDefinitionRegistry.get(dataSourceType);
    }

    @Override
    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        return dataRetrieverChainDefinitionRegistry.get(dataSourceType, retrievedDataType);
    }

    @Override
    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition(UUID id) {
        return dataRetrieverChainDefinitionRegistry.get(id);
    }
    
    @Override
    public <DataSourceType, ResultType> QueryDefinition<DataSourceType, ResultType> getQueryDefinitionForDTO(QueryDefinitionDTO queryDefinitionDTO) {
        ModifiableQueryDefinition<DataSourceType, ResultType> queryDefinition = null;
        
        Locale locale = DataMiningStringMessages.Util.getLocaleFor(queryDefinitionDTO.getLocaleInfoName());
        DataRetrieverChainDefinition<DataSourceType> retrieverChain = getDataRetrieverChainDefinition(queryDefinitionDTO.getDataRetrieverChainDefinition().getId());
        @SuppressWarnings("unchecked")
        Function<ResultType> statisticToCalculate = (Function<ResultType>) getFunctionForDTO(queryDefinitionDTO.getStatisticToCalculate());
        
        if (locale != null && retrieverChain != null && statisticToCalculate != null) {
             queryDefinition = new ModifiableQueryDefinition<>(locale, retrieverChain, statisticToCalculate, queryDefinitionDTO.getAggregatorType());
             
             for (Entry<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> levelSpecificFilterSelection : queryDefinitionDTO.getFilterSelection().entrySet()) {
                Integer retrieverLevel = levelSpecificFilterSelection.getKey();
                for (Entry<FunctionDTO, Collection<? extends Serializable>> levelSpecificFilterSelectionEntry : levelSpecificFilterSelection.getValue().entrySet()) {
                    Function<?> dimensionToFilterBy = getFunctionForDTO(levelSpecificFilterSelectionEntry.getKey());
                    if (dimensionToFilterBy != null) {
                        queryDefinition.setFilterSelection(retrieverLevel, dimensionToFilterBy, levelSpecificFilterSelectionEntry.getValue());
                    }
                }
            }
             
            for (FunctionDTO dimensionToGroupByDTO : queryDefinitionDTO.getDimensionsToGroupBy()) {
                Function<?> dimensionToGroupBy = getFunctionForDTO(dimensionToGroupByDTO);
                if (dimensionToGroupBy != null) {
                    queryDefinition.addDimensionToGroupBy(dimensionToGroupBy);
                }
            }
        }
        
        return queryDefinition;
    }

    @Override
    public <DataSource, DataType, ResultType> Query<ResultType> createQuery(DataSource dataSource, QueryDefinition<DataSource, ResultType> queryDefinition) {
        return new ProcessorQuery<ResultType, DataSource>(dataSource, stringMessages, queryDefinition.getLocale()) {
            @Override
            protected Processor<DataSource, ?> createFirstProcessor() {
                ProcessorFactory processorFactory = new ProcessorFactory(getExecutorService());
                
                Function<ResultType> extractionFunction = queryDefinition.getStatisticToCalculate();
                @SuppressWarnings("unchecked")
                Class<DataType> dataTypeToRetrieve = (Class<DataType>) extractionFunction.getDeclaringType();
                
                Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>> aggregationProcessor = processorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType(), queryDefinition.getResultType());
                Processor<GroupedDataEntry<DataType>, GroupedDataEntry<ResultType>> extractionProcessor = processorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                Processor<DataType, GroupedDataEntry<DataType>> groupingProcessor = processorFactory.createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, queryDefinition.getDimensionsToGroupBy());

                DataRetrieverChainBuilder<DataSource> chainBuilder = queryDefinition.getDataRetrieverChainDefinition().startBuilding(getExecutorService());
                Map<Integer, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(queryDefinition.getFilterSelection());
                while (chainBuilder.canStepFurther()) {
                    chainBuilder.stepFurther();
                    
                    if (criteriaMappedByRetrieverLevel.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
                        chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(chainBuilder.getCurrentRetrieverLevel()));
                    }
                }
                chainBuilder.addResultReceiver(groupingProcessor);
                
                return chainBuilder.build();
            }
            
        };
    }
    
    @SuppressWarnings("unchecked")
    private <DataType> Map<Integer, FilterCriterion<?>> createFilterCriteria(Map<Integer, Map<Function<?>, Collection<?>>> filterSelection) {
        Map<Integer, CompoundFilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<Integer, Map<Function<?>, Collection<?>>> levelFilterSelection : filterSelection.entrySet()) {
            for (Entry<Function<?>, Collection<?>> levelFilterSelectionEntry : levelFilterSelection.getValue().entrySet()) {
                Function<?> function = levelFilterSelectionEntry.getKey();
                Class<DataType> dataType = (Class<DataType>) function.getDeclaringType();
                
                if (!criteriaMappedByRetrieverLevel.containsKey(levelFilterSelection.getKey())) {
                    criteriaMappedByRetrieverLevel.put(levelFilterSelection.getKey(), new AndCompoundFilterCriterion<>(dataType));
                }

                Collection<Object> filterValues = new ArrayList<>(levelFilterSelectionEntry.getValue());
                ((CompoundFilterCriterion<DataType>) criteriaMappedByRetrieverLevel.get(levelFilterSelection.getKey())).addCriteria(new NullaryFunctionValuesFilterCriterion<>(dataType, function, filterValues));
            }
        }
        return (Map<Integer, FilterCriterion<?>>)(Map<Integer, ?>) criteriaMappedByRetrieverLevel;
    }

    @Override
    public <DataSource> Query<Set<Object>> createDimensionValuesQuery(DataSource dataSource,
            DataRetrieverChainDefinition<DataSource> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Locale locale) {
        return new ProcessorQuery<Set<Object>, DataSource>(dataSource, getStringMessages(), locale) {
            @Override
            protected Processor<DataSource, ?> createFirstProcessor() {
                ProcessorFactory processorFactory = new ProcessorFactory(getExecutorService());
                
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);

                DataRetrieverChainBuilder<DataSource> chainBuilder = dataRetrieverChainDefinition.startBuilding(getExecutorService());
                chainBuilder.stepFurther(); //Initialization
                for (int level = 0; level < retrieverLevel; level++) {
                    chainBuilder.stepFurther();
                }
                for (Processor<?, ?> resultReceiver : processorFactory.createGroupingExtractorsForDimensions(
                        chainBuilder.getCurrentRetrievedDataType(), valueCollector, dimensions, getStringMessages(), locale)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                return chainBuilder.build();
            }
        };
    }
    
}
