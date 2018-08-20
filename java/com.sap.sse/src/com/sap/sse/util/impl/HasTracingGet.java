package com.sap.sse.util.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public interface HasTracingGet<V> {
    /**
     * Keeps calling {@link Future#get(long, java.util.concurrent.TimeUnit)} with a timeout until a result is available
     * or an exception other than a {@link TimeoutException} is thrown. When a timeout occurs, some trace output is
     * written to the log. The method is guaranteed not to call {@link Future#get()} and therefore can be used in an
     * implementation of a {@link Future#get()} method without causing an endless recursion as long as
     * {@link Future#get(long, java.util.concurrent.TimeUnit)} is not calling this method.
     */
    V callGetAndTraceAfterEachTimeout(Future<V> future) throws InterruptedException, ExecutionException;
}
