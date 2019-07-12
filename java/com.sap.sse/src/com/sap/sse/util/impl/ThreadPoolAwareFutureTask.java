package com.sap.sse.util.impl;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.util.ThreadPoolUtil;

/**
 * A future task that outputs trace messages to the log after {@link #MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET} milliseconds
 * of a {@link #get()} call not returning. When used in conjunction with a {@link NamedTracingScheduledThreadPoolExecutor} such as
 * the ones returned by {@link ThreadPoolUtil}, the task will be updated by the executor when added to the queue. When the {@link #get()}
 * method doesn't respond after the timeout period, the executor that scheduled this tasks, its name as well as its queue size
 * are traced into the log.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <V>
 */
public class ThreadPoolAwareFutureTask<V> extends FutureTask<V> implements KnowsExecutor {
    private static final Logger logger = Logger.getLogger(ThreadPoolAwareFutureTask.class.getName());
    private final KnowsExecutorAndTracingGet<V> getHelper = new KnowsExecutorAndTracingGetImpl<V>();
    private final Object callableOrRunnableIfLoggingFine;
    private final Object delegate; // either a Callable or a Runnable whose type may or may not conform to the KnowsExecutor interface
    
    public ThreadPoolAwareFutureTask(ThreadPoolExecutor executor, Callable<V> callable) {
        super(callable);
        delegate = callable;
        if (logger.isLoggable(Level.FINE)) {
            callableOrRunnableIfLoggingFine = callable;
        } else {
            callableOrRunnableIfLoggingFine = null;
        }
        setExecutorThisTaskIsScheduledFor(executor);
    }

    public ThreadPoolAwareFutureTask(ThreadPoolExecutor executor, Runnable runnable, V result) {
        super(runnable, result);
        delegate = runnable;
        if (logger.isLoggable(Level.FINE)) {
            callableOrRunnableIfLoggingFine = runnable;
        } else {
            callableOrRunnableIfLoggingFine = null;
        }
        setExecutorThisTaskIsScheduledFor(executor);
    }
    
    @Override
    public void run() {
        getHelper.setInheritableThreadLocalValues();
        try {
            super.run();
        } finally {
            getHelper.removeInheritableThreadLocalValues();
        }
    }
    
    @Override
    public void setExecutorThisTaskIsScheduledFor(ThreadPoolExecutor executorThisTaskIsScheduledFor) {
        if (delegate instanceof KnowsExecutor) {
            ((KnowsExecutor) delegate).setExecutorThisTaskIsScheduledFor(executorThisTaskIsScheduledFor);
        }
        this.getHelper.setExecutorThisTaskIsScheduledFor(executorThisTaskIsScheduledFor);
    }

    @Override
    public Map<InheritableThreadLocal<Object>, Object> getThreadLocalValuesToInherit() {
        return getHelper.getThreadLocalValuesToInherit();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return getHelper.callGetAndTraceAfterEachTimeout(this);
    }
    
    @Override
    public String toString() {
        return super.toString()+(callableOrRunnableIfLoggingFine==null?"":(" "+callableOrRunnableIfLoggingFine.toString()));
    }
}
