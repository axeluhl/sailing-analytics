package com.sap.sse.util.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.sap.sse.util.ThreadPoolUtil;

public class ThreadPoolUtilImpl implements ThreadPoolUtil {
    private static final int REASONABLE_THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors()-1, 3);

    private final ScheduledExecutorService defaultBackgroundTaskThreadPoolExecutor;
    private final ScheduledExecutorService defaultForegroundTaskThreadPoolExecutor;
    
    public ThreadPoolUtilImpl() {
        defaultBackgroundTaskThreadPoolExecutor = Executors.newScheduledThreadPool(/* corePoolSize */ REASONABLE_THREAD_POOL_SIZE,
                new ThreadFactoryWithPriority(Thread.NORM_PRIORITY-1, /* daemon */ true));
        defaultForegroundTaskThreadPoolExecutor = Executors.newScheduledThreadPool(/* corePoolSize */ REASONABLE_THREAD_POOL_SIZE,
                new ThreadFactoryWithPriority(Thread.NORM_PRIORITY, /* daemon */ true));
    }
    
    @Override
    public ScheduledExecutorService getDefaultBackgroundTaskThreadPoolExecutor() {
        return defaultBackgroundTaskThreadPoolExecutor;
    }

    @Override
    public ScheduledExecutorService getDefaultForegroundTaskThreadPoolExecutor() {
        return defaultForegroundTaskThreadPoolExecutor;
    }

    @Override
    public ScheduledExecutorService createBackgroundTaskThreadPoolExecutor() {
        return createThreadPoolExecutor(Thread.NORM_PRIORITY-1);
    }

    @Override
    public ScheduledExecutorService createForegroundTaskThreadPoolExecutor() {
        return createThreadPoolExecutor(Thread.NORM_PRIORITY);
    }

    private ScheduledExecutorService createThreadPoolExecutor(final int priority) {
        return Executors.newScheduledThreadPool(/* corePoolSize */ REASONABLE_THREAD_POOL_SIZE,
                new ThreadFactoryWithPriority(priority, /* daemon */ true));
    }

    @Override
    public int getReasonableThreadPoolSize() {
        return REASONABLE_THREAD_POOL_SIZE;
    }
}
