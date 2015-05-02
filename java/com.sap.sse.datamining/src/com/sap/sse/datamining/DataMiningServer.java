package com.sap.sse.datamining;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;


public interface DataMiningServer {
    
    public ExecutorService getExecutorService();
    public ResourceBundleStringMessages getStringMessages();
    public FunctionProvider getFunctionProvider();

    public Date getComponentsChangedTimepoint();

    public Iterable<Function<?>> getAllStatistics();
    public Iterable<Function<?>> getFunctionsFor(Class<?> sourceType);
    public Iterable<Function<?>> getStatisticsFor(Class<?> sourceType);
    public Iterable<Function<?>> getDimensionsFor(Class<?> sourceType);
    public Iterable<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    /**
     * @return The first function, that matches the given DTO or <code>null</code>
     */
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO);

    public DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider();

    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getDataRetrieverChainDefinitionsBySourceType(
            Class<DataSourceType> dataSourceType);

    public <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getDataRetrieverChainDefinitionsByDataType(
            Class<DataType> dataType);

    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinition(UUID id);
    
    public <DataSourceType, DataType, ResultType> QueryDefinition<DataSourceType, DataType, ResultType> getQueryDefinitionForDTO(QueryDefinitionDTO queryDefinitionDTO);

    public <DataSourceType> Query<Set<Object>> createDimensionValuesQuery(DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Map<Integer, Map<Function<?>, Collection<?>>> filterSelection, Locale locale);

    public <DataSourceType, ResultType> Query<ResultType> createQuery(QueryDefinition<DataSourceType, ?, ResultType> queryDefinition);
    
    public <ResultType> QueryResult<ResultType> runNewQueryAndAbortPreviousQueries(DataMiningSession session, Query<ResultType> query);
    
}
