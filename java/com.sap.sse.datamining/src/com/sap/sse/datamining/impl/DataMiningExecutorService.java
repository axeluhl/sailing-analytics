package com.sap.sse.datamining.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataMiningExecutorService extends ThreadPoolExecutor {
    public DataMiningExecutorService(int corePoolSize) {
        super(corePoolSize, corePoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(corePoolSize),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        r.run();
                    }
                });
    }

}
