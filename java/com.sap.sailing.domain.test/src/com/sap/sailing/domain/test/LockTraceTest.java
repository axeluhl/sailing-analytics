package com.sap.sailing.domain.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class LockTraceTest {
    @Test
    public void testLockTrace() throws InterruptedException {
        NamedReentrantReadWriteLock lock1 = new NamedReentrantReadWriteLock("Lock1", /* fair */ true);
        lock1.readLock().lock();
        boolean itWorked = lock1.writeLock().tryLock(10, TimeUnit.MILLISECONDS);
        System.out.println(itWorked);
    }
}
