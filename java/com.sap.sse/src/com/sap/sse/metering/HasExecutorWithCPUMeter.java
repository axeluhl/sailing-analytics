package com.sap.sse.metering;

import java.util.concurrent.Callable;

import com.sap.sse.concurrent.RunnableWithException;

public interface HasExecutorWithCPUMeter extends ExecutorWithCPUMeter {
    ExecutorWithCPUMeter getExecutorWithCPUMeter();

    @Override
    default void runWithCPUMeter(Runnable runnable, String key) {
        getExecutorWithCPUMeter().runWithCPUMeter(runnable, key);
    }

    @Override
    default <T> T callWithCPUMeter(Callable<T> callable, String key) throws Exception {
        return getExecutorWithCPUMeter().callWithCPUMeter(callable, key);
    }

    @Override
    default <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException, String key) throws E {
        getExecutorWithCPUMeter().runWithCPUMeter(runnableWithException, key);
    }
}
