package com.sap.sailing.util.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.impl.Util;

public class LockUtil {
    private static final int NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK = 5;
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    
    /**
     * Bug <a href="http://bugs.sun.com/view_bug.do?bug_id=6822370">http://bugs.sun.com/view_bug.do?bug_id=6822370</a> seems
     * dangerous, particularly if it happens in a <code>LiveLeaderboardUpdater</code> thread. Even though the bug is reported to
     * have been fixed in JDK 7(b79) we should be careful. This method tries to acquire a lock, allowing for five seconds to pass.
     * After five seconds and not having retrieved the lock, tries again until the lock has been acquired.
     * @throws InterruptedException 
     */
    public static void lock(Lock lock, String lockDescriptionForTimeoutLogMessage) {
        boolean locked = false;
        boolean interrupted = false;
        while (!locked) {
            try {
                locked = lock.tryLock(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK, TimeUnit.SECONDS);
                if (!locked) {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    new Throwable("This is where the lock couldn't be acquired").printStackTrace(new PrintStream(
                            bos));
                    logger.info("Couldn't acquire lock "+lockDescriptionForTimeoutLogMessage+" in "+NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK+"s at "+
                            new String(bos.toByteArray())+"\nTrying again...");
                }
            }
            catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            // re-assert interrupt state that occurred while we
            // were acquiring the lock
            Thread.currentThread().interrupt();
        }
    }
    
    public static void lockForRead(NamedReentrantReadWriteLock lock) {
        lock(lock.readLock(), "readLock "+lock.getName());
    }
    
    public static void unlockAfterRead(NamedReentrantReadWriteLock lock) {
        lock.readLock().unlock();
    }
    
    public static void lockForWrite(NamedReentrantReadWriteLock lock) {
        lock(lock.writeLock(), "writeLock "+lock.getName());
    }
    
    public static void unlockAfterWrite(NamedReentrantReadWriteLock lock) {
        lock.writeLock().unlock();
    }
}
