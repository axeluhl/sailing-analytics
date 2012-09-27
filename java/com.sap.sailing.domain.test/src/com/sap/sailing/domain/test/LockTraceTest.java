package com.sap.sailing.domain.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Test;

import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class LockTraceTest {
    @Test
    public void testLockTraceForMultipleReaders() throws InterruptedException {
        NamedReentrantReadWriteLock lock1 = new NamedReentrantReadWriteLock("Lock1", /* fair */ true);
        lock1.readLock().lock();
        Object o = createAndStartLockingThreadReturningObjectToNotifyInOrderToReleaseLockAndTerminateThread(lock1.readLock());
        boolean itWorked = lock1.writeLock().tryLock(10, TimeUnit.MILLISECONDS);
        System.out.println(itWorked);
        lock1.readLock().unlock();
        synchronized (o) {
            o.notifyAll();
        }
    }
    
    private Object createAndStartLockingThreadReturningObjectToNotifyInOrderToReleaseLockAndTerminateThread(final Lock lock) {
        final Thread thread = new Thread("Thread to lock "+lock) {
            public void run() {
                lock.lock();
                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                lock.unlock();
            }
        };
        thread.start();
        return thread;
    }
}
