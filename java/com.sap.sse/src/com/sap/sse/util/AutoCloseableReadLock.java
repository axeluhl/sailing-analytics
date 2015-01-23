package com.sap.sse.util;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;


public class AutoCloseableReadLock implements AutoCloseable {
    private final NamedReentrantReadWriteLock lock;
    
    public AutoCloseableReadLock(NamedReentrantReadWriteLock lock) {
        this.lock = lock;
        LockUtil.lockForRead(lock);
    }

    @Override
    public void close() {
        LockUtil.unlockAfterRead(lock);
    }

}
