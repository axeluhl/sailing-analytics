package com.sap.sailing.util.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Map<Thread, Map<Lock, Integer>> lockCounts = new ConcurrentHashMap<Thread, Map<Lock,Integer>>();
    
    /**
     * Remembers which lock counts have been propagated from the A thread of the key pair to the B thread of the key pair.
     * This is important to remember because after calling {@link #propagateLockSet(Thread, Thread)} the <code>from</code> thread
     * may still acquire new locks before the <code>to</code> thread calls {@link #unpropagateLockSetFrom(Thread)}, and therefore
     * the lock set held by the <code>from</code> thread can grow. (Note that it cannot shrink because the guarantee expressed by
     * propagating locks is that after the propagation the <code>from</code> thread does not release any locks until the locks
     * are again unpropagated.
     */
    private static final Map<Util.Pair<Thread, Thread>, Map<Lock, Integer>> propagated = new ConcurrentHashMap<Util.Pair<Thread,Thread>, Map<Lock,Integer>>();
    
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
        if (!ifIsInCurrentThreadsLockSetThenIncrementLockCount(lock.readLock())) {
            lock(lock.readLock(), lock.getReadLockName(), lock);
            incrementLockCountForCurrentThread(lock.readLock());
        }
    }
    
    private static boolean ifIsInCurrentThreadsLockSetThenIncrementLockCount(Lock lock) {
        final boolean result;
        synchronized (getCurrentThreadsLockCounts()) {
            if (isInCurrentThreadsLockSet(lock)) {
                result = true;
                incrementLockCountForCurrentThread(lock);
            } else {
                result = false;
            }
        }
        return result;
    }
    
    private static Map<Lock, Integer> getCurrentThreadsLockCounts() {
        final Thread currentThread = Thread.currentThread();
        return getLockCounts(currentThread);
    }

    private static Map<Lock, Integer> getLockCounts(final Thread thread) {
        // don't synchronize all the frequent read accesses
        Map<Lock, Integer> result = lockCounts.get(thread);
        if (result == null) {
            // but if we need to create a new entry, ensure that this doesn't happen concurrently
            synchronized (lockCounts) {
                result = lockCounts.get(thread);
                if (result == null) {
                    result = new HashMap<Lock, Integer>();
                    lockCounts.put(thread, result);
                }
            }
        }
        return result;
    }
    private static void incrementLockCountForCurrentThread(Lock lock) {
        Map<Lock, Integer> map = getCurrentThreadsLockCounts();
        synchronized (map) {
            final int newValue;
            if (map.containsKey(lock)) {
                newValue = map.get(lock) + 1;
            } else {
                newValue = 1;
            }
            map.put(lock, newValue);
        }
    }
    
    private static void decrementLockCountForCurrentThread(Lock lock) {
        Map<Lock, Integer> map = getCurrentThreadsLockCounts();
        synchronized (map) {
            assert map.containsKey(lock);
            final int newValue = map.get(lock) - 1;
            if (newValue == 0) {
                map.remove(lock);
            } else {
                map.put(lock, newValue);
            }
        }
    }

    private static boolean isInCurrentThreadsLockSet(Lock lock) {
        return getCurrentThreadsLockCounts().containsKey(lock);
    }

    public static void unlockAfterRead(NamedReentrantReadWriteLock lock) {
        assert isInCurrentThreadsLockSet(lock.readLock());
        if (getCurrentThreadsLockCountSynchronzied(lock.readLock()) == 1) {
            lock.readLock().unlock();
            decrementLockCountForCurrentThread(lock.readLock());
        } else {
            decrementLockCountForCurrentThread(lock.readLock());
        }
    }
    
    public static void lockForWrite(NamedReentrantReadWriteLock lock) {
        if (!ifIsInCurrentThreadsLockSetThenIncrementLockCount(lock.writeLock())) {
            lock(lock.writeLock(), lock.getWriteLockName(), lock);
            incrementLockCountForCurrentThread(lock.writeLock());
            synchronized (lastTimeWriteLockWasObtained) {
                lastTimeWriteLockWasObtained.put(lock, MillisecondsTimePoint.now());
            }
        }
    }
    
    private static int getCurrentThreadsLockCountSynchronzied(Lock lock) {
        Map<Lock, Integer> currentThreadLockCounts = getCurrentThreadsLockCounts();
        final int result;
        synchronized (currentThreadLockCounts) {
            result = currentThreadLockCounts.get(lock);
        }
        return result;
    }
    
    public static void unlockAfterWrite(NamedReentrantReadWriteLock lock) {
        assert isInCurrentThreadsLockSet(lock.writeLock());
        if (getCurrentThreadsLockCountSynchronzied(lock.writeLock()) == 1) {
            lock.writeLock().unlock();
            decrementLockCountForCurrentThread(lock.writeLock());
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
        } else {
            decrementLockCountForCurrentThread(lock.writeLock());
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
     * ATTENTION: Calling this method makes a very strong assertion! It asserts that the calling thread will call
     * {@link #unpropagateLockSetFrom(Thread)} for the same <code>from</code> thread passed to this call before
     * <code>from</code> releases any of the locks it currently holds. The effect of making this call is that the
     * calling thread, when trying to acquire a lock already held by <code>from</code>, will not actually acquire that
     * lock again. This, in particular, has the effect that a read lock already held by <code>from</code> will not
     * have to be acquired again, which in turn avoids a read-read deadlock on a <em>fair</em> lock in case another
     * thread is attempting to acquire the corresponding write lock before the current thread tries to re-acquire the
     * read lock.
     * <p>
     * 
     * Always use this in a <code>try/finally</code> combination where in the <code>finally</code> block you call
     * {@link #unpropagateLockSetFrom(Thread)}.
     */
    public static void propagateLockSetFrom(Thread from) {
        Thread to = Thread.currentThread();
        propagateLockSet(from, to);
    }

    private static void propagateLockSet(Thread from, Thread to) {
        Map<Lock, Integer> fromMap = lockCounts.get(from);
        if (fromMap != null) {
            // first synchronize fromMap, then toMap; this way, no deadlock can occur as long as propagation works in the same direction
            synchronized (fromMap) {
                Map<Lock, Integer> propagatedLockCounts = new HashMap<Lock, Integer>(fromMap);
                propagated.put(new Util.Pair<Thread, Thread>(from, to), propagatedLockCounts);
                Map<Lock, Integer> toMap = getLockCounts(to);
                synchronized (toMap) {
                    for (Map.Entry<Lock, Integer> otherEntry : fromMap.entrySet()) {
                        if (toMap.containsKey(otherEntry.getKey())) {
                            toMap.put(otherEntry.getKey(), toMap.get(otherEntry.getKey()) + otherEntry.getValue());
                        } else {
                            toMap.put(otherEntry.getKey(), otherEntry.getValue());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * ATTENTION: Calling this method makes a very strong assertion! It asserts that the calling thread will not call
     * {@link #unpropagateLockSetTo(Thread)} for the same <code>to</code> thread passed to this call before the calling
     * thread releases any of the locks it currently holds. The effect of making this call is that the <code>to</code>
     * thread, when trying to acquire a lock already held by the calling thread, will not actually acquire that lock
     * again. This, in particular, has the effect that a read lock already held by the calling thread will not have to
     * be acquired again by <code>to</code>, which in turn avoids a read-read deadlock on a <em>fair</em> lock in case
     * another thread is attempting to acquire the corresponding write lock before <code>to</code> tries to re-acquire
     * the read lock.
     * <p>
     * 
     * Always use this in a <code>try/finally</code> combination where in the <code>finally</code> block you call
     * {@link #unpropagateLockSetTo(Thread)}.
     */
    public static void propagateLockSetTo(Thread to) {
        Thread from = Thread.currentThread();
        propagateLockSet(from, to);
    }

    /**
     * A thread that previously propagated the lock set from another thread by using {@link #propagateLockSetFrom(Thread)}
     * ends the propagation with this call. After this call, <code>thread</code> is free to release any locks it held
     * at the time {@link #propagateLockSetFrom(Thread)} was called by the current thread with <code>thread</code> as
     * the argument.
     */
    public static void unpropagateLockSetFrom(Thread from) {
        Thread to = Thread.currentThread();
        unpropagateLockSet(from, to);
    }

    private static void unpropagateLockSet(Thread from, Thread to) {
        Map<Lock, Integer> fromMap = propagated.get(new Util.Pair<Thread, Thread>(from, to));
        if (fromMap != null) {
            synchronized (fromMap) {
                Map<Lock, Integer> toMap = getLockCounts(to);
                synchronized (toMap) {
                    for (Map.Entry<Lock, Integer> otherEntry : fromMap.entrySet()) {
                        assert toMap.containsKey(otherEntry.getKey());
                        toMap.put(otherEntry.getKey(), toMap.get(otherEntry.getKey()) - otherEntry.getValue());
                    }
                }
            }
        }
    }
    
    /**
     * A thread that previously propagated the lock set to another thread by using {@link #propagateLockSetTo(Thread)}
     * ends the propagation with this call. After this call, the current thread is free to release any locks it held
     * at the time {@link #propagateLockSetTo(Thread)} was called by the current thread with <code>to</code> as
     * the argument.
     */
    public static void unpropagateLockSetTo(Thread to) {
        Thread from = Thread.currentThread();
        unpropagateLockSet(from, to);
    }
}
