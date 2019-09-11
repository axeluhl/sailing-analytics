package com.sap.sse.util.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.util.ThreadPoolUtil;

public class ThreadPoolUtilImpl implements ThreadPoolUtil {
    private static final Logger logger = Logger.getLogger(ThreadPoolUtilImpl.class.getName());
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

    @Override
    public void logExceptionsFromFutures(Level logLevel, String messageTemplate, Iterable<? extends Future<?>> futures) {
        for (final Future<?> result : futures) {
            try {
                result.get();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                logger.log(logLevel, String.format(messageTemplate, t.getMessage()), t);
            } catch (InterruptedException e) {
                logger.log(logLevel, String.format(messageTemplate, e.getMessage()), e);
            }
        }
    }

    @Override
    public <T> List<Future<T>> invokeAllAndLogExceptions(ExecutorService executor, Level logLevel, String messageTemplate,
            Iterable<? extends Callable<T>> tasks) {
        final List<Callable<T>> tasksAsList = new ArrayList<>();
        Util.addAll(tasks, tasksAsList);
        List<Future<T>> result = null;
        try {
            result = executor.invokeAll(tasksAsList);
            logExceptionsFromFutures(logLevel, messageTemplate, result);
        } catch (InterruptedException e) {
            logger.log(logLevel, String.format(messageTemplate, e.getMessage()), e);
        }
        return result;
    }
}
