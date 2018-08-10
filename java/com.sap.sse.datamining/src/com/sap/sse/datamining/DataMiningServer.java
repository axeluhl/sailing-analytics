package com.sap.sse.datamining;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionProvider;
import com.sap.sse.datamining.components.management.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.components.management.FunctionProvider;
import com.sap.sse.datamining.components.management.QueryDefinitionDTOProvider;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.impl.components.management.ReducedDimensions;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;


public interface DataMiningServer {
    
    ExecutorService getExecutorService();
    ResourceBundleStringMessages getStringMessages();

    Date getComponentsChangedTimepoint();

    FunctionProvider getFunctionProvider();
    Function<?> getIdentityFunction();
    Iterable<Function<?>> getAllStatistics();
    Iterable<Function<?>> getFunctionsFor(Class<?> sourceType);
    Iterable<Function<?>> getStatisticsFor(Class<?> sourceType);
    Iterable<Function<?>> getDimensionsFor(Class<?> sourceType);
    Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getDimensionsMappedByLevelFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    ReducedDimensions getReducedDimensionsMappedByLevelFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    Function<?> getFunctionForDTO(FunctionDTO functionDTO);

    DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider();
    Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions();
    <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getDataRetrieverChainDefinitionsBySourceType(Class<DataSourceType> dataSourceType);
    <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getDataRetrieverChainDefinitionsByDataType(Class<DataType> retrievedDataType);
    <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> getDataRetrieverChainDefinitions(Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType);
    <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinitionForDTO(DataRetrieverChainDefinitionDTO retrieverChainDTO);
    
    <DataSourceType> Query<HashSet<Object>> createDimensionValuesQuery(DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, DataRetrieverLevel<?, ?> retrieverLevel,
     Iterable<Function<?>> dimensions, Map<DataRetrieverLevel<?, ?>, SerializableSettings> settings, Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection, Locale locale);
    <DataSourceType, DataType, ExtractedType, ResultType> StatisticQueryDefinition<DataSourceType, DataType, ExtractedType, ResultType> getQueryDefinitionForDTO(StatisticQueryDefinitionDTO queryDefinitionDTO);
    <DataSourceType, ResultType> Query<ResultType> createQuery(StatisticQueryDefinition<DataSourceType, ?, ?, ResultType> queryDefinition);
    <ResultType> QueryResult<ResultType> runNewQueryAndAbortPreviousQueries(DataMiningSession session, Query<ResultType> query);
    int getNumberOfRunningQueries();
    
    AggregationProcessorDefinitionProvider getAggregationProcessorProvider();
    <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getAggregationProcessorDefinitions(Class<ExtractedType> extractedType);
    <ExtractedType> AggregationProcessorDefinition<? super ExtractedType, ?> getAggregationProcessorDefinition(Class<ExtractedType> extractedType, String aggregationNameMessageKey);
    <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getAggregationProcessorDefinitionForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO);
    
    QueryDefinitionDTOProvider getQueryDefinitionDTOProvider();
    Iterable<PredefinedQueryIdentifier> getPredefinedQueryIdentifiers();
    ModifiableStatisticQueryDefinitionDTO getPredefinedQueryDefinitionDTO(PredefinedQueryIdentifier identifier);
    
}
