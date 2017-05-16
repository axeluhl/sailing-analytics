package com.sap.sse.util.impl;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sse.util.ThreadPoolUtil;

/**
 * Can be used with a {@link Future} task and outputs trace messages to the log after
 * {@link #MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET} milliseconds of a {@link #get()} call not returning. When used
 * in conjunction with a {@link NamedTracingScheduledThreadPoolExecutor} such as the ones returned by
 * {@link ThreadPoolUtil}, the task will be
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <V>
 */
public class KnowsExecutorAndTracingGetImpl<V> extends HasTracingGetImpl<V> implements KnowsExecutorAndTracingGet<V> {
    private ThreadPoolExecutor executorThisTaskIsScheduledFor;

    @Override
    public void setExecutorThisTaskIsScheduledFor(ThreadPoolExecutor executorThisTaskIsScheduledFor) {
        this.executorThisTaskIsScheduledFor = executorThisTaskIsScheduledFor;
    }

    @Override
    protected String getAdditionalTraceInfo() {
        return executorThisTaskIsScheduledFor == null ? "not scheduled"
                : ("scheduled with executor " + executorThisTaskIsScheduledFor);
    }
}
