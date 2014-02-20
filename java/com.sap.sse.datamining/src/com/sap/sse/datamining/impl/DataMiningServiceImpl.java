package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.DataMiningService;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public class DataMiningServiceImpl implements DataMiningService {

    private FunctionRegistry functionRegistry;
    private FunctionProvider functionProvider;

    public DataMiningServiceImpl(FunctionRegistry functionRegistry, FunctionProvider functionProvider) {
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
    
}
