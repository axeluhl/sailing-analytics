package com.sap.sse.util;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;


public class AutoCloseableWriteLock implements AutoCloseable {
    private final NamedReentrantReadWriteLock lock;
    
    public AutoCloseableWriteLock(NamedReentrantReadWriteLock lock) {
        this.lock = lock;
        LockUtil.lockForWrite(lock);
    }

    @Override
    public void close() {
        LockUtil.unlockAfterWrite(lock);
    }

}
