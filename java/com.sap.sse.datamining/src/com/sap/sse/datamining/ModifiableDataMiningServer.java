package com.sap.sse.datamining;

import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface ModifiableDataMiningServer extends DataMiningServer {

    public void addStringMessages(ResourceBundleStringMessages stringMessages);
    public void removeStringMessages(ResourceBundleStringMessages stringMessages);

    public FunctionRegistry getFunctionRegistry();

    public void registerAllWithInternalFunctionPolicy(Iterable<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan);

    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister);

    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition);
    
}
