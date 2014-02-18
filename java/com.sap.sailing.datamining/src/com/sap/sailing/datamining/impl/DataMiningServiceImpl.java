package com.sap.sailing.datamining.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.DataMiningService;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public class DataMiningServiceImpl implements DataMiningService {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

//    private FunctionRegistry functionRegistry;
    private FunctionProvider functionProvider;

    public DataMiningServiceImpl(FunctionRegistry functionRegistry, FunctionProvider functionProvider) {
//        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        return functionProvider;
    }
    
    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
    
}
