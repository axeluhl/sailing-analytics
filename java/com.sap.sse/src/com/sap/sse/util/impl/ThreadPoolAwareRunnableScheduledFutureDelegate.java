package com.sap.sse.util.impl;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ThreadPoolAwareRunnableScheduledFutureDelegate<V> extends KnowsExecutorAndTracingGetImpl<V> implements KnowsExecutorAndTracingGet<V>, RunnableScheduledFuture<V> {
    private final RunnableScheduledFuture<V> future;

    public ThreadPoolAwareRunnableScheduledFutureDelegate(ThreadPoolExecutor executor, RunnableScheduledFuture<V> future) {
        this.future = future;
        setExecutorThisTaskIsScheduledFor(executor);
    }

    public long getDelay(TimeUnit unit) {
        return future.getDelay(unit);
    }

    public void run() {
        setInheritableThreadLocalValues();
        try {
            future.run();
        } finally {
            removeInheritableThreadLocalValues();
        }
    }

    public boolean isPeriodic() {
        return future.isPeriodic();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public int compareTo(Delayed o) {
        return future.compareTo(o);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public V get() throws InterruptedException, ExecutionException {
        return callGetAndTraceAfterEachTimeout(future);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public void setExecutorThisTaskIsScheduledFor(ThreadPoolExecutor executorThisTaskIsScheduledFor) {
        if (future instanceof KnowsExecutor) {
            // transitively announce the executor to the contained future:
            ((KnowsExecutor) future).setExecutorThisTaskIsScheduledFor(executorThisTaskIsScheduledFor);
        }
        super.setExecutorThisTaskIsScheduledFor(executorThisTaskIsScheduledFor);
    }
}
