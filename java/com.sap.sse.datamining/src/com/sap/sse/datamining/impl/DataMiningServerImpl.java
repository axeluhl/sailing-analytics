package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class DataMiningServerImpl implements DataMiningServer {
    
    private final DataMiningStringMessages stringMessages;
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;

    public DataMiningServerImpl(DataMiningStringMessages stringMessages, FunctionRegistry functionRegistry, FunctionProvider functionProvider) {
        this.stringMessages = stringMessages;
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
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
    
}
