package com.sap.sailing.util;

import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

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
