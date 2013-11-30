package com.sap.sailing.util;

import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

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
