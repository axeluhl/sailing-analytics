package com.sap.sse.util.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * A scheduled thread pool executor with a name that produces {@link Future} tasks that write a trace message
 * to the log after their {@link Future#get()} method hasn't produced a result for some time, then keep trying.
 * The tasks are linked to this executor, also when a {@link KnowsExecutor} task is submitted to the {@link #execute(Runnable)}
 * method, so that when they need to trace they
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class NamedTracingScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private final String name;
    
    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        this.name = name;
    }

    public NamedTracingScheduledThreadPoolExecutor(String name, int corePoolSize) {
        super(corePoolSize);
        this.name = name;
    }
    
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return new ThreadPoolAwareRunnableScheduledFutureDelegate<>(this, task);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return new ThreadPoolAwareRunnableScheduledFutureDelegate<>(this, task);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ThreadPoolAwareFutureTask<T>(this, runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ThreadPoolAwareFutureTask<T>(this, callable);
    }

    @Override
    public void execute(Runnable command) {
        if (command instanceof KnowsExecutor) {
            ((KnowsExecutor) command).setExecutorThisTaskIsScheduledFor(this);
        }
        super.execute(command);
    }

    @Override
    public String toString() {
        return "NamedTracingScheduledThreadPoolExecutor [name=" + name + "]";
    }
}
