package com.sap.sse.datamining.impl;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataMiningExecutorService extends ThreadPoolExecutor {
    
    public DataMiningExecutorService(int corePoolSize) {
        super(corePoolSize, corePoolSize, 60, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
    }

}
