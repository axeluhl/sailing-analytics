package com.sap.sailing.util.impl;

import java.util.HashMap;
import java.util.Map;
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
    private static final Map<NamedReentrantReadWriteLock, TimePoint> lastTimeWriteLockWasObtained = new WeakHashMap<NamedReentrantReadWriteLock, TimePoint>();
    private static final Map<Thread, Map<Lock, Integer>> lockCounts = new HashMap<Thread, Map<Lock,Integer>>();
    
    /**
     * Bug <a href="http://bugs.sun.com/view_bug.do?bug_id=6822370">http://bugs.sun.com/view_bug.do?bug_id=6822370</a> seems
     * dangerous, particularly if it happens in a <code>LiveLeaderboardUpdater</code> thread. Even though the bug is reported to
     * have been fixed in JDK 7(b79) we should be careful. This method tries to acquire a lock, allowing for five seconds to pass.
     * After five seconds and not having retrieved the lock, tries again until the lock has been acquired.
     */
    private static void lock(Lock lock, String lockDescriptionForTimeoutLogMessage, NamedReentrantReadWriteLock lockParent) {
        boolean locked = false;
        boolean interrupted = false;
        while (!locked) {
            try {
                locked = lock.tryLock(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK, TimeUnit.SECONDS);
                if (!locked) {
                    StringBuilder message = new StringBuilder();
                    message.append("Couldn't acquire lock ");
                    message.append(lockDescriptionForTimeoutLogMessage);
                    message.append(" in ");
                    message.append(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK);
                    message.append("s in thread "+Thread.currentThread().getName()+" at ");
                    message.append(getCurrentStackTrace());
                    Thread writer = lockParent.getWriter();
                    if (writer != null) {
                        message.append("\nThe current writer is:\n");
                        appendThreadData(message, writer);
                    }
                    message.append("\nThe current readers are:\n");
                    for (Thread reader : lockParent.getReaders()) {
                        appendThreadData(message, reader);
                    }
                    message.append("Trying again...");
                    logger.info(message.toString());
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

    private static void appendThreadData(StringBuilder message, Thread writer) {
        message.append(writer);
        message.append('\n');
        message.append(getStackTrace(writer));
        message.append('\n');
    }
    
    public static void lockForRead(NamedReentrantReadWriteLock lock) {
        if (isInCurrentThreadsLockSet(lock.readLock())) {
            incrementLockCountForCurrentThread(lock.readLock());
        } else {
            lock(lock.readLock(), lock.getReadLockName(), lock);
            addToCurrentThreadsLockSet(lock.readLock());
        }
    }
    
    private static Map<Lock, Integer> getCurrentThreadsLockCounts() {
        final Thread currentThread = Thread.currentThread();
        return getLockCounts(currentThread);
    }

    private static Map<Lock, Integer> getLockCounts(final Thread thread) {
        Map<Lock, Integer> result = lockCounts.get(thread);
        if (result == null) {
            result = new HashMap<Lock, Integer>();
            lockCounts.put(thread, result);
        }
        return result;
    }
    private static void incrementLockCountForCurrentThread(Lock lock) {
        Map<Lock, Integer> map = getCurrentThreadsLockCounts();
        assert map.containsKey(lock);
        map.put(lock, map.get(lock) + 1);
    }
    
    private static void decrementLockCountForCurrentThread(Lock lock) {
        Map<Lock, Integer> map = getCurrentThreadsLockCounts();
        assert map.containsKey(lock);
        map.put(lock, map.get(lock)-1);
    }

    private static boolean isInCurrentThreadsLockSet(Lock lock) {
        return getCurrentThreadsLockCounts().containsKey(lock);
    }

    private static void addToCurrentThreadsLockSet(Lock lock) {
        getCurrentThreadsLockCounts().put(lock, 1);
    }
    
    private static void removeFromCurrentThreadsLockSet(Lock lock) {
        getCurrentThreadsLockCounts().remove(lock);
    }

    public static void unlockAfterRead(NamedReentrantReadWriteLock lock) {
        assert isInCurrentThreadsLockSet(lock.readLock());
        if (getCurrentThreadsLockCounts().get(lock.readLock()) == 1) {
            lock.readLock().unlock();
            removeFromCurrentThreadsLockSet(lock.readLock());
        } else {
            decrementLockCountForCurrentThread(lock.readLock());
        }
    }
    
    public static void lockForWrite(NamedReentrantReadWriteLock lock) {
        lock(lock.writeLock(), lock.getWriteLockName(), lock);
        synchronized (lastTimeWriteLockWasObtained) {
            lastTimeWriteLockWasObtained.put(lock, MillisecondsTimePoint.now());
        }
    }
    
    public static void unlockAfterWrite(NamedReentrantReadWriteLock lock) {
        lock.writeLock().unlock();
        final TimePoint timePointWriteLockWasObtained;
        synchronized (lastTimeWriteLockWasObtained) {
            timePointWriteLockWasObtained = lastTimeWriteLockWasObtained.get(lock);
        }
        if (timePointWriteLockWasObtained == null) {
            logger.info("Internal error: write lock "+lock.getName()+" to be unlocked but no time recorded for when it was last obtained.\n"+
                    "This is where the lock interaction happened:\n"+getCurrentStackTrace());
        } else {
            TimePoint now = MillisecondsTimePoint.now();
            if (now.asMillis()-timePointWriteLockWasObtained.asMillis() > 10000l) {
                String stackTrace = getCurrentStackTrace();
                logger.info("write lock "+lock.getName()+" was held for more than 10s. It got unlocked here: "+
                        stackTrace);
            }
        }
    }
    
    private static String getStackTrace(Thread thread) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement sf : thread.getStackTrace()) {
            sb.append(sf.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String getCurrentStackTrace() {
        return getStackTrace(Thread.currentThread());
    }

    /**
     * ATTENTION: Calling this method makes a very strong assertion! It asserts that the calling thread
     * will call {@link #unpropagateLockSetFrom(Thread)} for the same <code>thread</code> passed to this
     * call before <code>thread</code> releases any of the locks it currently holds. The effect of making
     * this call is that the calling thread, when trying to acquire a lock already held by <code>thread</code>,
     * will not actually acquire that lock again. This, in particular, has the effect that a read lock
     * already held by <code>thread</code> will not have to be acquired again, which in turn avoids a
     * read-read deadlock on a fair lock in case another thread is attempting to acquire the corresponding
     * write lock before the current thread tries to re-acquire the read lock.
     */
    public static void propagateLockSetFrom(Thread thread) {
        Map<Lock, Integer> otherMap = lockCounts.get(thread);
        Map<Lock, Integer> currentMap = getCurrentThreadsLockCounts();
        for (Map.Entry<Lock, Integer> otherEntry : otherMap.entrySet()) {
            if (currentMap.containsKey(otherEntry.getKey())) {
                currentMap.put(otherEntry.getKey(), currentMap.get(otherEntry.getKey()) + otherEntry.getValue());
            } else {
                currentMap.put(otherEntry.getKey(), otherEntry.getValue());
            }
        }
    }

    /**
     * A thread that previously propagated the lock set from another thread by using {@link #propagateLockSetFrom(Thread)}
     * ends the propagation with this call. After this call, <code>thread</code> is free to release any locks it held
     * at the time {@link #propagateLockSetFrom(Thread)} was called by the current thread with <code>thread</code> as
     * the argument.
     */
    public static void unpropagateLockSetFrom(Thread thread) {
        Map<Lock, Integer> otherMap = lockCounts.get(thread);
        Map<Lock, Integer> currentMap = getCurrentThreadsLockCounts();
        for (Map.Entry<Lock, Integer> otherEntry : otherMap.entrySet()) {
            assert currentMap.containsKey(otherEntry.getKey());
            currentMap.put(otherEntry.getKey(), currentMap.get(otherEntry.getKey()) - otherEntry.getValue());
        }
    }
}
