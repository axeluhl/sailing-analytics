package com.sap.sse.datamining;

import java.util.Collection;

import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public interface ModifiableDataMiningServer extends DataMiningServer {

    public void addStringMessages(DataMiningStringMessages stringMessages);
    public void removeStringMessages(DataMiningStringMessages stringMessages);

    public FunctionRegistry getFunctionRegistry();

    public void registerAllWithInternalFunctionPolicy(Collection<Class<?>> classesToScan);
    public void registerAllWithExternalFunctionPolicy(Collection<Class<?>> externalClassesToScan);

    public void unregisterAllFunctionsOf(Collection<Class<?>> classesToUnregister);

    public void registerDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);
    public void unregisterDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition);
    
}
