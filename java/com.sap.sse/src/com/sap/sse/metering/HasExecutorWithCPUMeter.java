package com.sap.sse.metering;

import com.sap.sse.concurrent.RunnableWithException;
import com.sap.sse.concurrent.RunnableWithResult;
import com.sap.sse.concurrent.RunnableWithResultAndException;

public interface HasExecutorWithCPUMeter extends ExecutorWithCPUMeter {
    ExecutorWithCPUMeter getExecutorWithCPUMeter();

    @Override
    default void runWithCPUMeter(Runnable runnable, String key) {
        getExecutorWithCPUMeter().runWithCPUMeter(runnable, key);
    }

    @Override
    default <T, E extends Throwable> T callWithCPUMeterWithException(RunnableWithResultAndException<T, E> callable, String key) throws E {
        return getExecutorWithCPUMeter().callWithCPUMeterWithException(callable, key);
    }

    @Override
    default <T> T callWithCPUMeter(RunnableWithResult<T> callable, String key) {
        return getExecutorWithCPUMeter().callWithCPUMeter(callable, key);
    }

    @Override
    default <E extends Exception> void runWithCPUMeter(RunnableWithException<E> runnableWithException, String key) throws E {
        getExecutorWithCPUMeter().runWithCPUMeter(runnableWithException, key);
    }
}
