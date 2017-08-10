package com.sap.sse.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.util.impl.ApproximateTime;

/**
 * Supports lock management for {@link NamedReentrantReadWriteLock} which is a specialization of
 * {@link ReentrantReadWriteLock} that provides enhanced tracing capabilities. This class offers a number of utility
 * methods which also support the propagation of locks from one thread to another. This can be used by threads to
 * testify that they will wait for another thread which therefore doesn't need to acquire the same locks again. This can
 * help to avoid deadlocks.
 * <p>
 * 
 * Locks are propagated from one thread to another in the form of counters managed in the {@link #propagationCounts} map that
 * count, how many other threads have propagated their lock held. When a thread needs to obtain a lock, it first checks
 * if this lock has a positive count in the map. If so, the lock doesn't need to actually be locked because some other
 * thread has asserted that it holds the lock already. Instead, a counter is incremented in the {@link #virtualLockCounts}
 * map that is used to monitor that the "virtual" locks are handled properly and that all of them have been unlocked
 * when unpropagation happens.
 * <p>
 * 
 * Unlocking is a bit trickier for read locks because even if a thread A holds the read lock, a thread B may still
 * obtain it. If A then propagates its locks to B, B needs to understand whether it last "obtained" the lock by just
 * incrementing the counter on the propagated lock, or by actually obtaining the lock itself, and then perform the
 * inverse operation. Note that lock propagation and unpropagation may happen more or less at any point in time
 * (synchronized on the receiving thread's {@link #propagationCounts} entry), so thread B may first actually obtain the read
 * lock, then receive propagated locks from thread A, then reentrantly lock again by incrementing the counter. The only
 * guarantee that users of the {@link LockUtil} class need to provide is that the locks are unpropagated only after
 * thread B is done with its locking operations as long as A waits for B. This in particular means that B is required to
 * unlock all locks it obtained (actually or virtually by incrementing the counter) before unpropagation happens. Note
 * also that more than one thread (A_1, A_2, ...) may propagate its locks to the same other thread B.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class LockUtil {
    private enum ReadOrWrite { READ, WRITE };
    
    /**
     * Can be replaced with java.util.function.Supplier when we can consistently use Java 8.
     */
    public interface RunnableWithResult<T> {
        T run();
    }
    
    private static final int NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK = 5;
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static final Map<NamedReentrantReadWriteLock, TimePoint> lastTimeWriteLockWasObtained = new ConcurrentWeakHashMap<NamedReentrantReadWriteLock, TimePoint>();
    
    /**
     * Tells how many other threads propagated which held lock to the key thread. During propagation, a lock is
     * considered held if it is really locked by the propagating thread or if the propagating thread received it itself
     * through propagation. The value maps only contain positive (non-zero) values. If a count goes to zero, the
     * corresponding key lock is removed from the map.
     * <p>
     * 
     * The thread-specific value maps are used as monitor objects whenever decisions about thread-specific lock counts
     * need to be made.
     */
    private static final Map<Thread, Map<Lock, Integer>> propagationCounts = new ConcurrentWeakHashMap<Thread, Map<Lock,Integer>>();
    
    /**
     * Counts the "virtual" locks. A "virtual" lock is obtained if and only if at the time of calling
     * {@link #lockForRead(NamedReentrantReadWriteLock)} or {@link #lockForWrite(NamedReentrantReadWriteLock)} the
     * respective lock has a positive {@link #propagationCounts propagation count} for the current thread. Entries
     * always have a positive integer value. {@link #decrement(Lock, Map)} removes entries whose integer count would
     * go to 0.
     */
    private static final Map<Thread, Map<Lock, Integer>> virtualLockCounts = new ConcurrentWeakHashMap<Thread, Map<Lock, Integer>>();
    
    /**
     * Redundant but easily accessible hold count per thread and lock. These are the actual lock hold counts as they are
     * recorded in the actual {@link NamedReentrantReadWriteLock} locks.
     */
    private static final Map<Thread, Map<Lock, Integer>> lockCounts = new ConcurrentWeakHashMap<Thread, Map<Lock, Integer>>();
    
    public static void lockForRead(NamedReentrantReadWriteLock lock) {
        acquireLockVirtuallyOrActually(lock, lock.readLock(), ReadOrWrite.READ);
    }

    public static void lockForWrite(NamedReentrantReadWriteLock lock) {
        acquireLockVirtuallyOrActually(lock, lock.writeLock(), ReadOrWrite.WRITE);
        lastTimeWriteLockWasObtained.put(lock, ApproximateTime.approximateNow());
    }
    
    private static void acquireLockVirtuallyOrActually(NamedReentrantReadWriteLock lock, final Lock readOrWriteLock, final ReadOrWrite readOrWrite) {
        boolean locked = false;
        while (!locked) {
            final Map<Lock, Integer> currentThreadsPropagationCounts = getCurrentThreadsPropagationCounts();
            synchronized (currentThreadsPropagationCounts) {
                if (currentThreadsPropagationCounts.containsKey(readOrWriteLock)) {
                    // read lock was propagated to current thread by at least one other thread; use virtual lock by incrementing counter:
                    incrementVirtualLockCountForCurrentThread(readOrWriteLock);
                    // Uncomment the following in case of issues you want to debug with logging:
                    // logger.finest("only incremented virtual count for "+readOrWrite+" lock " + lock.getName() + " in thread "
                    //       + Thread.currentThread().getName()+" to "+getCurrentThreadsVirtualLockCounts().get(readOrWriteLock));
                    locked = true;
                } else {
                    // lock was not yet propagated; try to actually obtain lock
                    locked = lock(readOrWriteLock, readOrWrite==ReadOrWrite.READ ? lock.getReadLockName() : lock.getWriteLockName(), lock);
                    if (locked) {
                        // Uncomment the following in case of issues you want to debug with logging:
                        // logger.finest("actually acquired "+readOrWrite+" lock " + lock.getName() + " in thread " + Thread.currentThread().getName());
                        incrementLockCountForCurrentThread(readOrWriteLock);
                    }
                }
            }
            if (!locked) {
                Thread.yield(); // let any other thread that is trying to propagate locks get access to currentThreadsPropagationCounts
            }
        }
    }
    
    public static void unlockAfterRead(NamedReentrantReadWriteLock lock) {
        final ReadLock readOrWriteLock = lock.readLock();
        unlockVirtuallyOrActually(lock, readOrWriteLock);
    }

    private static void unlockVirtuallyOrActually(NamedReentrantReadWriteLock lock, final Lock readOrWriteLock) {
        Map<Lock, Integer> currentThreadPropagationCounts = getCurrentThreadsPropagationCounts();
        synchronized (currentThreadPropagationCounts) {
            assert isInCurrentThreadsLockSet(readOrWriteLock);
            Integer virtualLockCount = getCurrentThreadsVirtualLockCounts().get(readOrWriteLock);
            if (virtualLockCount != null) {
                // an entry is a positive entry and means the current thread acquired the lock virtually; unlock virtually again
                decrementVirtualLockCountForCurrentThread(readOrWriteLock);
                // Uncomment the following in case of issues you want to debug with logging:
                // logger.finest("only decremented virtual count for "+readOrWrite+" lock "+lock.getName()+" in thread "+Thread.currentThread().getName()+" from "+virtualLockCount);
            } else {
                // Uncomment the following in case of issues you want to debug with logging:
                // logger.finest("actually unlocking "+readOrWrite+" lock "+lock.getName()+" in thread "+Thread.currentThread().getName());
                readOrWriteLock.unlock();
                decrementLockCountForCurrentThread(readOrWriteLock);
            }
        }
    }
    
    public static void unlockAfterWrite(NamedReentrantReadWriteLock lock) {
        unlockVirtuallyOrActually(lock, lock.writeLock());
        final TimePoint timePointWriteLockWasObtained;
        timePointWriteLockWasObtained = lastTimeWriteLockWasObtained.get(lock);
        if (timePointWriteLockWasObtained == null) {
            logger.info("Internal error: write lock " + lock.getName()
                    + " to be unlocked but no time recorded for when it was last obtained.\n"
                    + "This is where the lock interaction happened:\n" + getCurrentStackTrace());
        } else {
            TimePoint now = ApproximateTime.approximateNow();
            final Duration heldWriteLockForMillis = timePointWriteLockWasObtained.until(now);
            if (heldWriteLockForMillis.compareTo(Duration.ONE_SECOND.times(10)) > 0) {
                String stackTrace = getCurrentStackTrace();
                logger.info("write lock " + lock.getName() + " was approximately held for more than 10s (" + heldWriteLockForMillis
                        + "). It got unlocked here: " + stackTrace);
            }
        }
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

    /**
     * Propagates all locks that <code>from</code> received itself through propagation and those that <code>from</code> actually
     * locked itself. If <code>from</code> received a lock through propagation <em>and</em> actually holds it, it is only
     * propagated once.
     */
    private static void propagateLockSet(Thread from, Thread to) {
        Set<Lock> locksToPropagate = getLocksHeldVirtuallyOrActuallyBy(from);
        Map<Lock, Integer> toMap = getPropagationCounts(to);
        synchronized (toMap) {
            for (Lock lockToPropagate : locksToPropagate) {
                increment(lockToPropagate, toMap);
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
        Set<Lock> locksToPropagate = getLocksHeldVirtuallyOrActuallyBy(from);
        Map<Lock, Integer> toMap = getPropagationCounts(to);
        synchronized (toMap) {
            for (Lock lockToPropagate : locksToPropagate) {
                decrement(lockToPropagate, toMap);
            }
        }
    }

    private static Set<Lock> getLocksHeldVirtuallyOrActuallyBy(Thread thread) {
        Set<Lock> locksToPropagate = new HashSet<Lock>();
        Map<Lock, Integer> propagationMap = propagationCounts.get(thread);
        if (propagationMap != null) {
            // first synchronize fromMap, then toMap; this way, no deadlock can occur as long as propagation works in the same direction
            synchronized (propagationMap) {
                for (Map.Entry<Lock, Integer> otherEntry : propagationMap.entrySet()) {
                    locksToPropagate.add(otherEntry.getKey());
                }
            }
        }
        Map<Lock, Integer> lockMap = lockCounts.get(thread);
        if (lockMap != null) {
            for (Map.Entry<Lock, Integer> otherEntry : lockMap.entrySet()) {
                locksToPropagate.add(otherEntry.getKey());
            }
        }
        return locksToPropagate;
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

    private static Map<Lock, Integer> getCurrentThreadsPropagationCounts() {
        final Thread currentThread = Thread.currentThread();
        return getPropagationCounts(currentThread);
    }

    private static Map<Lock, Integer> getCurrentThreadsLockCounts() {
        final Thread currentThread = Thread.currentThread();
        return getLockCounts(currentThread);
    }

    private static Map<Lock, Integer> getCurrentThreadsVirtualLockCounts() {
        final Thread currentThread = Thread.currentThread();
        return getVirtualLockCounts(currentThread);
    }

    private static Map<Lock, Integer> getOrCreateMapForThread(final Thread thread, Map<Thread, Map<Lock, Integer>> map) {
        // don't synchronize all the frequent read accesses
        Map<Lock, Integer> result = map.get(thread);
        if (result == null) {
            // but if we need to create a new entry, ensure that this doesn't happen concurrently
            synchronized (map) {
                result = map.get(thread);
                if (result == null) {
                    result = new ConcurrentHashMap<Lock, Integer>();
                    map.put(thread, result);
                }
            }
        }
        return result;
    }

    private static Map<Lock, Integer> getLockCounts(final Thread thread) {
        return getOrCreateMapForThread(thread, lockCounts);
    }

    private static Map<Lock, Integer> getPropagationCounts(final Thread thread) {
        return getOrCreateMapForThread(thread, propagationCounts);
    }

    private static Map<Lock, Integer> getVirtualLockCounts(final Thread thread) {
        return getOrCreateMapForThread(thread, virtualLockCounts);
    }

    private static void incrementLockCountForCurrentThread(Lock lock) {
        Map<Lock, Integer> map = getCurrentThreadsLockCounts();
        increment(lock, map);
    }

    private static void decrementLockCountForCurrentThread(Lock lock) {
        decrement(lock, getCurrentThreadsLockCounts());
    }

    private static void incrementVirtualLockCountForCurrentThread(Lock lock) {
        increment(lock, getCurrentThreadsVirtualLockCounts());
    }
    
    private static void decrementVirtualLockCountForCurrentThread(Lock lock) {
        decrement(lock, getCurrentThreadsVirtualLockCounts());
    }

    private static void increment(Lock lock, Map<Lock, Integer> map) {
        final int newValue;
        if (map.containsKey(lock)) {
            newValue = map.get(lock) + 1;
        } else {
            newValue = 1;
        }
        map.put(lock, newValue);
    }
    
    private static void decrement(Lock lock, Map<Lock, Integer> map) {
        assert map.containsKey(lock);
        final int newValue = map.get(lock) - 1;
        if (newValue == 0) {
            map.remove(lock);
        } else {
            map.put(lock, newValue);
        }
    }

    private static boolean isInCurrentThreadsLockSet(Lock lock) {
        return getCurrentThreadsLockCounts().containsKey(lock) || getCurrentThreadsVirtualLockCounts().containsKey(lock);
    }

    
    private static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement sf : stackTrace) {
            sb.append(sf.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String getCurrentStackTrace() {
        return formatStackTrace(Thread.currentThread().getStackTrace());
    }

    /**
     * Actually obtains read or write lock <code>lock</code> of the {@link NamedReentrantReadWriteLock} owner lock.<p>
     * 
     * Bug <a href="http://bugs.sun.com/view_bug.do?bug_id=6822370">http://bugs.sun.com/view_bug.do?bug_id=6822370</a> seems
     * dangerous, particularly if it happens in a <code>LiveLeaderboardUpdater</code> thread. Even though the bug is reported to
     * have been fixed in JDK 7(b79) we should be careful. This method tries to acquire a lock, allowing for five seconds to pass.
     * After five seconds and not having retrieved the lock, tries again until the lock has been acquired.
     */
    private static boolean lock(Lock lock, String lockDescriptionForTimeoutLogMessage, NamedReentrantReadWriteLock lockParent) {
        boolean locked = false;
        try {
            locked = lock.tryLock(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK, TimeUnit.SECONDS);
            if (!locked) {
                Thread writer = lockParent.getWriter();
                // capture the stack traces as quickly as possible to try to reflect the situation as it was when the lock couuldn't be obtained
                StackTraceElement[] writerStackTrace = writer != null ? writer.getStackTrace() : null;
                Map<Thread, StackTraceElement[]> readerStackTraces = new HashMap<Thread, StackTraceElement[]>();
                final Iterable<Thread> readers = lockParent.getReaders();
                for (Thread reader : readers) {
                    readerStackTraces.put(reader, reader.getStackTrace());
                }
                StringBuilder message = new StringBuilder();
                message.append("Couldn't acquire lock ");
                message.append(lockDescriptionForTimeoutLogMessage);
                message.append(" in ");
                message.append(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK);
                message.append("s in thread " + Thread.currentThread().getName() + " at ");
                message.append(getCurrentStackTrace());
                if (writer != null) {
                    message.append("\nThe current writer is:\n");
                    appendThreadData(message, writer, writerStackTrace);
                }
                if (readers != null && !Util.isEmpty(readers)) {
                    message.append("\nThe current readers are:\n");
                    for (Thread reader : readers) {
                        appendThreadData(message, reader, readerStackTraces.get(reader));
                    }
                }
                message.append("Trying again...");
                logger.info(message.toString());
            }
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, "Interrupted while waiting for lock "+lockDescriptionForTimeoutLogMessage, ex);
        }
        return locked;
    }

    private static void appendThreadData(StringBuilder message, Thread writer, StackTraceElement[] stackTrace) {
        message.append(writer);
        message.append('\n');
        message.append(formatStackTrace(stackTrace));
        message.append('\n');
    }

    /**
     * Convenience method to execute a {@link Runnable} while the given {@link NamedReentrantReadWriteLock} is locked
     * for read. Ensures, that unlock is done in a finally block.
     */
    public static void executeWithReadLock(NamedReentrantReadWriteLock lock, Runnable runnable) {
        lockForRead(lock);
        try {
            runnable.run();
        } finally {
            unlockAfterRead(lock);
        }
    }
    
    /**
     * Convenience method to execute a {@link RunnableWithResult} while the given {@link NamedReentrantReadWriteLock} is locked
     * for read. Ensures, that unlock is done in a finally block.
     */
    public static <T> T executeWithReadLockAndResult(NamedReentrantReadWriteLock lock, RunnableWithResult<T> runnable) {
        lockForRead(lock);
        try {
            return runnable.run();
        } finally {
            unlockAfterRead(lock);
        }
    }

    /**
     * Convenience method to execute a {@link Runnable} while the given {@link NamedReentrantReadWriteLock} is locked
     * for write. Ensures, that unlock is done in a finally block.
     */
    public static void executeWithWriteLock(NamedReentrantReadWriteLock lock, Runnable runnable) {
        lockForWrite(lock);
        try {
            runnable.run();
        } finally {
            unlockAfterWrite(lock);
        }
    }
    
    /**
     * Convenience method to execute a {@link Runnable} while the given {@link NamedReentrantReadWriteLock} is locked
     * for write. Ensures, that unlock is done in a finally block.
     */
    public static <T> T executeWithWriteLockAndResult(NamedReentrantReadWriteLock lock, RunnableWithResult<T> runnable) {
        lockForWrite(lock);
        try {
            return runnable.run();
        } finally {
            unlockAfterWrite(lock);
        }
    }
}
