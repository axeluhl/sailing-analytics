package com.sap.sse.datamining.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.DataRetrieverChainDefinitionRegistry;
import com.sap.sse.datamining.DataSourceProvider;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.factories.QueryFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.CompoundResourceBundleStringMessages;

public class DataMiningServerImpl implements ModifiableDataMiningServer {
    
    private final CompoundResourceBundleStringMessages stringMessages;
    private final ExecutorService executorService;
    private Date componentsChangedTimepoint;
    
    private final QueryFactory queryFactory;
    private final DataMiningQueryManager dataMiningQueryManager;
    
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;
    
    private final Map<Class<?>, DataSourceProvider<?>> dataSourceProviderMappedByDataSourceType;
    private final DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    public DataMiningServerImpl(ExecutorService executorService, FunctionRegistry functionRegistry, FunctionProvider functionProvider, DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry) {
        this.stringMessages = new CompoundResourceBundleStringMessages();
        this.executorService = executorService;
        componentsChangedTimepoint = new Date();
        this.queryFactory = new QueryFactory();
        dataMiningQueryManager = new StrategyPerQueryTypeManager();
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
        dataSourceProviderMappedByDataSourceType = new HashMap<>();
        this.dataRetrieverChainDefinitionRegistry = dataRetrieverChainDefinitionRegistry;
    }
    
    @Override
    public Date getComponentsChangedTimepoint() {
        return componentsChangedTimepoint;
    }
    
    private void updateComponentsChangedTimepoint() {
        componentsChangedTimepoint = new Date();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    @Override
    public ResourceBundleStringMessages getStringMessages() {
        return stringMessages;
    }

    @Override
    public void addStringMessages(ResourceBundleStringMessages stringMessages) {
        boolean componentsChanged = this.stringMessages.addStringMessages(stringMessages);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }

    @Override
    public void removeStringMessages(ResourceBundleStringMessages stringMessages) {
        boolean componentsChanged = this.stringMessages.removeStringMessages(stringMessages);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    @Override
    public void registerAllClasses(Iterable<Class<?>> classesToScan) {
        boolean componentsChanged = functionRegistry.registerAllClasses(classesToScan);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }
    
    @Override
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan) {
        boolean componentsChanged = functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }
    
    @Override
    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister) {
        boolean componentsChanged = functionRegistry.unregisterAllFunctionsOf(classesToUnregister);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
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
    public Iterable<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        return functionProvider.getDimensionsFor(dataRetrieverChainDefinition);
    }

    @Override
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO) {
        return functionProvider.getFunctionForDTO(functionDTO);
    }
    
    @Override
    public void setDataSourceProvider(DataSourceProvider<?> dataSourceProvider) {
        dataSourceProviderMappedByDataSourceType.put(dataSourceProvider.getDataSourceType(), dataSourceProvider);
        updateComponentsChangedTimepoint();
    }
    
    @Override
    public void removeDataSourceProvider(DataSourceProvider<?> dataSourceProvider) {
        boolean componentsChanged = dataSourceProviderMappedByDataSourceType.remove(dataSourceProvider.getDataSourceType()) != null;
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }
    
    @Override
    public DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider() {
        return dataRetrieverChainDefinitionRegistry;
    }
    
    @Override
    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        boolean componentsChanged = dataRetrieverChainDefinitionRegistry.register(dataRetrieverChainDefinition);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }
    
    @Override
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        boolean componentsChanged = dataRetrieverChainDefinitionRegistry.unregister(dataRetrieverChainDefinition);
        if (componentsChanged) {
            updateComponentsChangedTimepoint();
        }
    }
    
    @Override
    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getDataRetrieverChainDefinitionsBySourceType(
            Class<DataSourceType> dataSourceType) {
        return dataRetrieverChainDefinitionRegistry.getBySourceType(dataSourceType);
    }
    
    @Override
    public <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getDataRetrieverChainDefinitionsByDataType(
            Class<DataType> dataType) {
        return dataRetrieverChainDefinitionRegistry.getByDataType(dataType);
    }

    @Override
    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType) {
        return dataRetrieverChainDefinitionRegistry.get(dataSourceType, retrievedDataType);
    }

    @Override
    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinition(UUID id) {
        return dataRetrieverChainDefinitionRegistry.get(id);
    }
    
    @Override
    public <DataSourceType, DataType, ResultType> QueryDefinition<DataSourceType, DataType, ResultType> getQueryDefinitionForDTO(QueryDefinitionDTO queryDefinitionDTO) {
        ModifiableQueryDefinition<DataSourceType, DataType, ResultType> queryDefinition = null;
        
        Locale locale = ResourceBundleStringMessages.Util.getLocaleFor(queryDefinitionDTO.getLocaleInfoName());
        DataRetrieverChainDefinition<DataSourceType, DataType> retrieverChain = getDataRetrieverChainDefinition(queryDefinitionDTO.getDataRetrieverChainDefinition().getId());
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
    public <DataSourceType, ResultType> Query<ResultType> createQuery(QueryDefinition<DataSourceType, ?, ResultType> queryDefinition) {
        DataSourceProvider<DataSourceType> dataSourceProvider = getDataSourceProviderFor(queryDefinition.getDataRetrieverChainDefinition().getDataSourceType());
        return queryFactory.createQuery(dataSourceProvider.getDataSource(), queryDefinition, getStringMessages(), getExecutorService());
    }

    @Override
    public <DataSourceType> Query<Set<Object>> createDimensionValuesQuery(DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Map<Integer, Map<Function<?>, Collection<?>>> filterSelection, Locale locale) {
        DataSourceProvider<DataSourceType> dataSourceProvider = getDataSourceProviderFor(dataRetrieverChainDefinition.getDataSourceType());
        return queryFactory.createDimensionValuesQuery(dataSourceProvider.getDataSource(), dataRetrieverChainDefinition, retrieverLevel, dimensions, filterSelection, locale, getStringMessages(), getExecutorService());
    }

    @SuppressWarnings("unchecked")
    private <DataSourceType> DataSourceProvider<DataSourceType> getDataSourceProviderFor(Class<DataSourceType> dataSourceType) {
        assert dataSourceProviderMappedByDataSourceType.containsKey(dataSourceType) : "No DataSourceProvider found for '" + dataSourceType + "'";
        return (DataSourceProvider<DataSourceType>) dataSourceProviderMappedByDataSourceType.get(dataSourceType);
    }
    
    @Override
    public <ResultType> QueryResult<ResultType> runNewQueryAndAbortPreviousQueries(DataMiningSession session, Query<ResultType> query) {
        return dataMiningQueryManager.runNewAndAbortPrevious(session, query);
    }
    
}
