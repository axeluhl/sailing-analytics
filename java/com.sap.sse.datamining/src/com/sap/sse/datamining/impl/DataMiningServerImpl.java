package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class DataMiningServerImpl implements DataMiningServer {
    
    private final DataMiningStringMessages stringMessages;
    
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;
    
    private DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    public DataMiningServerImpl(DataMiningStringMessages stringMessages, FunctionRegistry functionRegistry, FunctionProvider functionProvider, DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry) {
        this.stringMessages = stringMessages;
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
        this.dataRetrieverChainDefinitionRegistry = dataRetrieverChainDefinitionRegistry;
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        return functionProvider;
    }
    
    @Override
    public DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }
    
    @Override
    public DataRetrieverChainDefinitionRegistry getDataRetrieverChainDefinitionRegistry() {
        return dataRetrieverChainDefinitionRegistry;
    }
    
}
