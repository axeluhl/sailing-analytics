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
    Function<?> getFunctionForDTO(FunctionDTO functionDTO);

    DataRetrieverChainDefinitionProvider getDataRetrieverChainDefinitionProvider();
    <DataSourceType, DataType> DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinitionForDTO(DataRetrieverChainDefinitionDTO retrieverChainDTO);
    
    AggregationProcessorDefinitionProvider getAggregationProcessorProvider();
    <ExtractedType, ResultType> AggregationProcessorDefinition<ExtractedType, ResultType> getAggregationProcessorDefinitionForDTO(AggregationProcessorDefinitionDTO aggregatorDefinitionDTO);
    
    QueryDefinitionDTOProvider getQueryDefinitionDTOProvider();
    
    <DataSourceType> Query<HashSet<Object>> createDimensionValuesQuery(DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, DataRetrieverLevel<?, ?> retrieverLevel,
     Iterable<Function<?>> dimensions, Map<DataRetrieverLevel<?, ?>, SerializableSettings> settings, Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection, Locale locale);
    <DataSourceType, DataType, ExtractedType, ResultType> StatisticQueryDefinition<DataSourceType, DataType, ExtractedType, ResultType> getQueryDefinitionForDTO(StatisticQueryDefinitionDTO queryDefinitionDTO);
    <DataSourceType, ResultType> Query<ResultType> createQuery(StatisticQueryDefinition<DataSourceType, ?, ?, ResultType> queryDefinition);
    <ResultType> QueryResult<ResultType> runNewQueryAndAbortPreviousQueries(DataMiningSession session, Query<ResultType> query);
    int getNumberOfRunningQueries();
    
    //-----------------------------------------------------------------------------------------------------------------
    // Component Accessors as default methods
    //-----------------------------------------------------------------------------------------------------------------

    // Functions ------------------------------------------------------------------------------------------------------
    
    default Function<?> getIdentityFunction() {
        return getFunctionProvider().getIdentityFunction();
    }
    
    default Iterable<Function<?>> getAllStatistics() {
        return getFunctionProvider().getAllStatistics();
    }
    
    default Iterable<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return getFunctionProvider().getFunctionsFor(sourceType);
    }
    
    default Iterable<Function<?>> getStatisticsFor(Class<?> sourceType) {
        return getFunctionProvider().getStatisticsFor(sourceType);
    }
    
    default Iterable<Function<?>> getDimensionsFor(Class<?> sourceType) {
        return getFunctionProvider().getDimensionsFor(sourceType);
    }
    
    default Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getDimensionsMappedByLevelFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        return getFunctionProvider().getDimensionsMappedByLevelFor(dataRetrieverChainDefinition);
    }
    
    default ReducedDimensions getReducedDimensionsMappedByLevelFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        return getFunctionProvider().getReducedDimensionsMappedByLevelFor(dataRetrieverChainDefinition);
    }
    
    // Retriever Chains -----------------------------------------------------------------------------------------------

    default Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return getDataRetrieverChainDefinitionProvider().getAll();
    }
    
    default <DataSourceType> Iterable<DataRetrieverChainDefinition<DataSourceType, ?>> getDataRetrieverChainDefinitionsBySourceType(Class<DataSourceType> dataSourceType) {
        return getDataRetrieverChainDefinitionProvider().getBySourceType(dataSourceType);
    }
    
    default <DataType> Iterable<DataRetrieverChainDefinition<?, DataType>> getDataRetrieverChainDefinitionsByDataType(Class<DataType> retrievedDataType) {
        return getDataRetrieverChainDefinitionProvider().getByDataType(retrievedDataType);
    }
    
    default <DataSourceType, DataType> Iterable<DataRetrieverChainDefinition<DataSourceType, DataType>> getDataRetrieverChainDefinitions(Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType) {
        return getDataRetrieverChainDefinitionProvider().get(dataSourceType, retrievedDataType);
    }
    
    // Aggregators ----------------------------------------------------------------------------------------------------
    
    default Iterable<AggregationProcessorDefinition<?, ?>> getAllAggregationProcessorDefinitions() {
        return getAggregationProcessorProvider().getAll();
    }

    default <ExtractedType> Iterable<AggregationProcessorDefinition<? super ExtractedType, ?>> getAggregationProcessorDefinitions(Class<ExtractedType> extractedType) {
        return getAggregationProcessorProvider().getByExtractedType(extractedType);
    }
    
    default <ExtractedType> AggregationProcessorDefinition<? super ExtractedType, ?> getAggregationProcessorDefinition(Class<ExtractedType> extractedType, String aggregationNameMessageKey) {
        return getAggregationProcessorProvider().get(extractedType, aggregationNameMessageKey);
    }
    
    // Predefined Queries ---------------------------------------------------------------------------------------------

    default Iterable<PredefinedQueryIdentifier> getPredefinedQueryIdentifiers() {
        return getQueryDefinitionDTOProvider().getIdentifiers();
    }
    
    default ModifiableStatisticQueryDefinitionDTO getPredefinedQueryDefinitionDTO(PredefinedQueryIdentifier identifier) {
        return getQueryDefinitionDTOProvider().get(identifier);
    }
    
}
