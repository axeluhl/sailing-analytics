package com.sap.sse.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import com.sap.sse.util.impl.ManagedBlockerWithReturnImpl;

public interface ManagedBlockerWithReturn<T> extends ManagedBlocker {
    T getResult();
    
    static <T> ManagedBlockerWithReturn<T> create(Callable<T> callable) {
        return new ManagedBlockerWithReturnImpl<T>(callable);
    }
}
