package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.util.AutoCloseableReadLock;
import com.sap.sailing.util.AutoCloseableWriteLock;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class LockAsResourceTest {
    @Test
    public void testTryWithReadLockAsResource() {
        NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("Test Lock", /* fair */ false);
        try (AutoCloseableReadLock readlock = new AutoCloseableReadLock(lock)) {
            assertEquals(1, lock.getReadHoldCount());
        }
        assertEquals(0, lock.getReadHoldCount());
    }

    @Test
    public void testTryWithWriteLockAsResource() {
        NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("Test Lock", /* fair */ false);
        try (AutoCloseableWriteLock readlock = new AutoCloseableWriteLock(lock)) {
            assertEquals(1, lock.getWriteHoldCount());
        }
        assertEquals(0, lock.getWriteHoldCount());
    }
}
