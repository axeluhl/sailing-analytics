package com.sap.sse.util.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureTaskWithTracingGet<V> extends FutureTask<V> {
    private final HasTracingGet<V> hasTracingGet;
    private final String traceInfo;
    
    public FutureTaskWithTracingGet(String traceInfo, Callable<V> callable) {
        super(callable);
        this.traceInfo = traceInfo;
        hasTracingGet = createHasTracingGet();
    }

    public FutureTaskWithTracingGet(String traceInfo, Runnable runnable, V result) {
        super(runnable, result);
        this.traceInfo = traceInfo;
        hasTracingGet = createHasTracingGet();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return hasTracingGet.callGetAndTraceAfterEachTimeout(this);
    }

    private HasTracingGet<V> createHasTracingGet() {
        return new HasTracingGetImpl<V>() {
            @Override
            protected String getAdditionalTraceInfo() {
                return traceInfo;
            }
        };
    }

}
