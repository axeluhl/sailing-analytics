package com.sap.sse.datamining.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sse.util.impl.ThreadFactoryWithPriority;

public class DataMiningExecutorService extends ThreadPoolExecutor {
    // FIXME bug4821: what about security session state with this executor? Where do we submit tasks to this executor, and what should their Shiro session state / Subject be?
    public DataMiningExecutorService(int corePoolSize) {
        super(corePoolSize, corePoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(corePoolSize),
                new ThreadFactoryWithPriority(Thread.NORM_PRIORITY, /* daemon */ true),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        r.run();
                    }
                });
    }
}
