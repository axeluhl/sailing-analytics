package com.sap.sse.datamining;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.impl.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;


public interface DataMiningServer {
    
    public ExecutorService getExecutorService();

    public ResourceBundleStringMessages getStringMessages();
    
    public FunctionProvider getFunctionProvider();

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

    public <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType);

    public <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> getDataRetrieverChainDefinitions(
            Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);

    public <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinition(UUID id);
    
    public <DataSourceType, DataType, ResultType> QueryDefinition<DataSourceType, DataType, ResultType> getQueryDefinitionForDTO(QueryDefinitionDTO queryDefinitionDTO);

    public <DataSource> Query<Set<Object>> createDimensionValuesQuery(DataSource dataSource,
            DataRetrieverChainDefinition<DataSource, ?> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Locale locale);

    public <DataSource, DataType, ResultType> Query<ResultType> createQuery(DataSource dataSource, QueryDefinition<DataSource, DataType, ResultType> queryDefinition);
    
}
