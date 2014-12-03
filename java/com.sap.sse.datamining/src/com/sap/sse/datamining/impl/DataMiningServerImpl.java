package com.sap.sse.datamining.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
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
    public QueryDefinition<?, ?> getQueryDefinitionForDTO(QueryDefinitionDTO queryDefinitionDTO) {
        ModifiableQueryDefinition<?, ?> queryDefinition = null;
        
        Locale locale = DataMiningStringMessages.Util.getLocaleFor(queryDefinitionDTO.getLocaleInfoName());
        DataRetrieverChainDefinition<?> retrieverChain = getDataRetrieverChainDefinition(queryDefinitionDTO.getDataRetrieverChainDefinition().getId());
        Function<?> statisticToCalculate = getFunctionForDTO(queryDefinitionDTO.getStatisticToCalculate());
        
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
    
}
