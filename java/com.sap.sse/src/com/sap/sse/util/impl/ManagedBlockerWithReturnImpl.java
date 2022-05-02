package com.sap.sse.util.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import com.sap.sse.util.ManagedBlockerWithReturn;

/**
 * Assumes that a potentially blocking {@link Callable} is to be invoked in what may be a thread of
 * a {@link ForkJoinPool}. It therefore wraps a callable as a {@link ManagedBlocker} which is announced
 * to always block ({@link #isReleasable()} always returning {@code false}).<p>
 * 
 * The result of invoking the callable can be obtained through the {@link #getResult()} method.<p>
 * 
 * Use as in
 * <pre>
 *   
 * </pre>
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public class ManagedBlockerWithReturnImpl<T> implements ManagedBlockerWithReturn<T> {
    private final Callable<T> callable;
    private T result;

    public ManagedBlockerWithReturnImpl(Callable<T> callable) {
        super();
        this.callable = callable;
    }

    @Override
    public boolean block() throws InterruptedException {
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return isReleasable();
    }

    @Override
    public boolean isReleasable() {
        return false;
    }

    @Override
    public T getResult() {
        return result;
    }
}
