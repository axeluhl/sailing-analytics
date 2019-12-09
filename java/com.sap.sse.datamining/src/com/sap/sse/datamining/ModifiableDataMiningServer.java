package com.sap.sse.datamining;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;
import com.sap.sse.datamining.components.management.DataRetrieverChainDefinitionRegistry;
import com.sap.sse.datamining.components.management.FunctionRegistry;
import com.sap.sse.datamining.components.management.QueryDefinitionDTORegistry;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.util.JoinedClassLoader;

public interface ModifiableDataMiningServer extends DataMiningServer {

    void addDataMiningBundleClassLoader(ClassLoader classLoader);
    void removeDataMiningBundleClassLoader(ClassLoader classLoader);
    JoinedClassLoader getJoinedClassLoader();

    void addStringMessages(ResourceBundleStringMessages stringMessages);
    void removeStringMessages(ResourceBundleStringMessages stringMessages);

    FunctionRegistry getFunctionRegistry();
    void registerAllClasses(Iterable<Class<?>> classesToScan);
    void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);
    void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);
    
    void registerDataSourceProvider(DataSourceProvider<?> dataSourceProvider);
    void unregisterDataSourceProvider(DataSourceProvider<?> dataSourceProvider);
    
    DataRetrieverChainDefinitionRegistry getDataRetrieverChainDefinitionRegistry();
    void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    
    AggregationProcessorDefinitionRegistry getAggregationProcessorRegistry();
    void registerAggregationProcessor(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    void unregisterAggregationProcessor(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    
    QueryDefinitionDTORegistry getQueryDefinitionDTORegistry();
    void registerPredefinedQueryDefinition(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition);
    void unregisterPredefinedQueryDefinition(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition);
    
}
