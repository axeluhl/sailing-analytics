package com.sap.sse.datamining;

import java.util.Collection;

import com.sap.sse.datamining.functions.FunctionRegistry;

public interface ModifiableDataMiningServer extends DataMiningServer {

    public FunctionRegistry getFunctionRegistry();

    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan);

    public void unregisterAllFunctionsOf(Collection<Class<?>> classesToUnregister);

    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);
    
}
