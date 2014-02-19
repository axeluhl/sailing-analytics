package com.sap.sse.datamining;

import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public interface DataMiningService {

    public FunctionRegistry getFunctionRegistry();

    public FunctionProvider getFunctionProvider();

    public ThreadPoolExecutor getExecutor();

}
