package com.sap.sse.util.impl;

import java.util.concurrent.ScheduledExecutorService;

import com.sap.sse.util.ThreadPoolUtil;

public class ThreadPoolUtilImpl implements ThreadPoolUtil {
    private static final int REASONABLE_THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors()-1, 3);

    private final ScheduledExecutorService defaultBackgroundTaskThreadPoolExecutor;
    private final ScheduledExecutorService defaultForegroundTaskThreadPoolExecutor;
    
    public ThreadPoolUtilImpl() {
        defaultBackgroundTaskThreadPoolExecutor = createBackgroundTaskThreadPoolExecutor("Default background executor");
        defaultForegroundTaskThreadPoolExecutor = createForegroundTaskThreadPoolExecutor(2*REASONABLE_THREAD_POOL_SIZE, "Default foreground executor");
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
    public ScheduledExecutorService createBackgroundTaskThreadPoolExecutor(String name) {
        return createThreadPoolExecutor(name, Thread.NORM_PRIORITY-1);
    }

    @Override
    public ScheduledExecutorService createBackgroundTaskThreadPoolExecutor(int size, String name) {
        return createThreadPoolExecutor(name, Thread.NORM_PRIORITY-1, size);
    }

    @Override
    public ScheduledExecutorService createForegroundTaskThreadPoolExecutor(String name) {
        return createThreadPoolExecutor(name, Thread.NORM_PRIORITY);
    }

    @Override
    public ScheduledExecutorService createForegroundTaskThreadPoolExecutor(int size, String name) {
        return createThreadPoolExecutor(name, Thread.NORM_PRIORITY, size);
    }

    private ScheduledExecutorService createThreadPoolExecutor(String name, final int priority) {
        return createThreadPoolExecutor(name, priority, /* corePoolSize */ REASONABLE_THREAD_POOL_SIZE);
    }

    private ScheduledExecutorService createThreadPoolExecutor(String name, final int priority, final int size) {
        return new NamedTracingScheduledThreadPoolExecutor(name, /* corePoolSize */ size, new ThreadFactoryWithPriority(
                name, priority, /* daemon */ true));
    }

    @Override
    public int getReasonableThreadPoolSize() {
        return REASONABLE_THREAD_POOL_SIZE;
    }
}
