package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
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
    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan) {
        functionRegistry.registerAllWithInternalFunctionPolicy(classesToScan);
    }
    
    @Override
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan) {
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Override
    public void unregisterAllFunctionsOf(Collection<Class<?>> classesToUnregister) {
        functionRegistry.unregisterAllFunctionsOf(classesToUnregister);
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        return functionProvider;
    }

    @Override
    public Collection<Function<?>> getAllStatistics() {
        return functionProvider.getAllStatistics();
    }

    @Override
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return functionProvider.getFunctionsFor(sourceType);
    }

    @Override
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType) {
        return functionProvider.getStatisticsFor(sourceType);
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType) {
        return functionProvider.getDimensionsFor(sourceType);
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
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
    public <DataSourceType> Collection<DataRetrieverChainDefinition<DataSourceType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<?> retrievedDataType) {
        return dataRetrieverChainDefinitionRegistry.get(dataSourceType, retrievedDataType);
    }

    @Override
    public <DataSourceType> DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition(
            Class<DataSourceType> dataSourceType, UUID id) {
        return dataRetrieverChainDefinitionRegistry.get(dataSourceType, id);
    }
    
}
