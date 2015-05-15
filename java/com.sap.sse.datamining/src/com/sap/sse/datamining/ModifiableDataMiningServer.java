package com.sap.sse.datamining;

import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface ModifiableDataMiningServer extends DataMiningServer {

    public void addStringMessages(ResourceBundleStringMessages stringMessages);
    public void removeStringMessages(ResourceBundleStringMessages stringMessages);

    public FunctionRegistry getFunctionRegistry();
    public void registerAllClasses(Iterable<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);
    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);
    
    public void setDataSourceProvider(DataSourceProvider<?> dataSourceProvider);
    public void removeDataSourceProvider(DataSourceProvider<?> dataSourceProvider);
    
    public DataRetrieverChainDefinitionRegistry getDataRetrieverChainDefinitionRegistry();
    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    
    public AggregationProcessorDefinitionRegistry getAggregationProcessorRegistry();
    public void registerAggregationProcessor(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    public void unregisterAggregationProcessor(AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition);
    
}
