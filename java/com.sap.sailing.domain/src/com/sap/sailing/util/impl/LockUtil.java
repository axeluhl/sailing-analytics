package com.sap.sailing.util.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;

public class LockUtil {
    private static final int NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK = 5;
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static final WeakHashMap<NamedReentrantReadWriteLock, TimePoint> lastTimeWriteLockWasObtained = new WeakHashMap<>();
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
                            getCurrentStackTrace()+"\nTrying again...");
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
        lastTimeWriteLockWasObtained.put(lock, MillisecondsTimePoint.now());
    }
    
    public static void unlockAfterWrite(NamedReentrantReadWriteLock lock) {
        lock.writeLock().unlock();
        TimePoint timePointWriteLockWasObtained = lastTimeWriteLockWasObtained.get(lock);
        if (timePointWriteLockWasObtained == null) {
            logger.info("Internal error: write lock "+lock.getName()+" to be unlocked but no time recorded for when it was last obtained.\n"+
                    getCurrentStackTrace());
        } else {
            TimePoint now = MillisecondsTimePoint.now();
            if (now.asMillis()-timePointWriteLockWasObtained.asMillis() > 10000l) {
                String stackTrace = getCurrentStackTrace();
                logger.info("write lock "+lock.getName()+" was held for more than 10s. It got unlocked here: "+
                        stackTrace);
            }
        }
    }

    private static String getCurrentStackTrace() {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Throwable("This is where the lock couldn't be acquired").printStackTrace(new PrintStream(
                bos));
        String stackTrace = new String(bos.toByteArray());
        return stackTrace;
    }
}
