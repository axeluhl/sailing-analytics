package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.util.SmartFutureCache;
import com.sap.sailing.util.SmartFutureCache.EmptyUpdateInterval;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * When the thread reading a value from the cache holds a fair read lock, and another thread is trying to obtain the
 * corresponding write lock, and the cache updating method tries to acquire the read lock as well, lock propagation
 * used to work only if the reader caused the synchronous triggering of the computing method. For asynchronous triggers,
 * a read-read deadlock could occur because no lock propagation was supported from the reading to the computing thread.
 * This test belongs to bug 1344 (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1344).<p>
 * 
 * There are two variants to be tested. Either the computing thread runs into the lock before the reader gets to call
 * {@link SmartFutureCache#get(Object, boolean)} and times out trying to obtain the read lock while the reader propagates
 * its locks so the computing thread gets it based on the propagation in the second try; or the computing thread hasn't
 * tried to obtain the lock yet. In this case, the propagation from the reading thread happens first, and the first
 * computing thread's attempt to obtain the read lock will succeed.<p>
 * 
 * Doing white-box testing, further variants include whether or not the reading thread synchronously triggers the computing
 * thread (e.g., with the cache in suspended mode and the reading thread demanding the latest results). If it does, the computing
 * thread will itself propagate the locks from the reading thread to the computing thread. A test should verify that this doesn't
 * conflict with how the lock propagation is triggered pro-actively by the reading thread.
 * 
 * @author Axel Uhl (D043530)
 */
public class SmartFutureCacheDeadlockTest {
    private static final Logger logger = Logger.getLogger(SmartFutureCacheDeadlockTest.class.getName());
    
    private static final String RESULT = "result";
    private static final String CACHE_KEY = "cacheKey";
    private NamedReentrantReadWriteLock lock;
    private Thread computingThread;
    private LockingScript reader;
    private Thread readerThread;
    private LockingScript writer;
    private Thread writerThread;
    private SmartFutureCache<String, String, EmptyUpdateInterval> sfc;

    @Before
    public void setUp() throws InterruptedException {
        lock = new NamedReentrantReadWriteLock("testReadReadDeadlockBetweenGetterAndTrigger", /* fair */ true);
        sfc = new SmartFutureCache<String, String, SmartFutureCache.EmptyUpdateInterval>(
                new SmartFutureCache.AbstractCacheUpdater<String, String, SmartFutureCache.EmptyUpdateInterval>() {
                    @Override
                    public String computeCacheUpdate(String key, EmptyUpdateInterval updateInterval) throws Exception {
                        computingThread = Thread.currentThread();
                        logger.info("Trying to obtain read lock for "+lock.getName()+" in thread "+computingThread.getName());
                        LockUtil.lockForRead(lock);
                        try {
                            logger.info("Successfully obtained read lock for "+lock.getName()+" in thread "+computingThread.getName());
                            return RESULT;
                        } finally {
                            LockUtil.unlockAfterRead(lock);
                            logger.info("Unlocked read lock for "+lock.getName()+" in thread "+computingThread.getName());
                        }
                    }
                }, "SmartFutureCacheTest.testSuspendAndResume");
        reader = new LockingScript(lock, sfc);
        readerThread = new Thread(reader, "readerThread");
        writer = new LockingScript(lock, sfc);
        writerThread = new Thread(writer, "writerThread");
        readerThread.start();
        writerThread.start();
    }
    
    @Test
    public void testBasicLockingAndUnlockingWithScripts() throws InterruptedException {
        assertFalse(lock.getReaders().contains(readerThread));
        reader.performAndWait(Command.LOCK_FOR_READ);
        assertTrue(lock.getReaders().contains(readerThread));
        writer.perform(Command.LOCK_FOR_WRITE);
        assertNull(lock.getWriter());
        reader.performAndWait(Command.UNLOCK_AFTER_READ);
        assertFalse(lock.getReaders().contains(readerThread));
        writer.performAndWait(Command.UNLOCK_AFTER_WRITE);
    }
    
    @Test
    public void testBasicCaching() {
        assertNull(computingThread);
        sfc.triggerUpdate(CACHE_KEY, /* updateInterval */ null);
        String result = sfc.get(CACHE_KEY, /* waitForLatest */ true);
        assertEquals(RESULT, result);
        assertNotNull(computingThread);
    }
    
    @Test
    public void testReadReadDeadlockBetweenGetterAndTriggerInSynchronousScenario() throws InterruptedException {
        long start = System.currentTimeMillis();
        sfc.suspend();
        sfc.triggerUpdate(CACHE_KEY, /* update interval */ null); // queues the update, but get(CACHE_KEY, true) will now trigger recalculation synchronously
        reader.performAndWait(Command.LOCK_FOR_READ);
        writer.perform(Command.LOCK_FOR_WRITE);
        // in suspended mode, the following will trigger a re-calculation immediately,
        // and the locks from the readerThread will be propagated to the computing thread
        reader.performAndWait(Command.GET_LATEST_FROM_CACHE);
        assertNotNull(computingThread);
        reader.performAndWait(Command.UNLOCK_AFTER_READ); // this shall unblock the writer
        writer.performAndWait(Command.UNLOCK_AFTER_WRITE);
        assertTrue(System.currentTimeMillis()-start < 5000); // must not take longer than 5s, otherwise a locking conflict must have occurred;
        // see also LockUtil.NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK
    }
    
    @Test
    public void testReadReadDeadlockBetweenGetterAndTriggerInAsynchronousScenario() throws InterruptedException {
        long start = System.currentTimeMillis();
        reader.performAndWait(Command.LOCK_FOR_READ);
        writer.perform(Command.LOCK_FOR_WRITE);
        writer.waitUntilWaitingForLock();
        sfc.triggerUpdate(CACHE_KEY, /* update interval */ null); // starts the update which 
        // in suspended mode, the following will trigger a re-calculation immediately,
        // and the locks from the readerThread will be propagated to the computing thread
        reader.performAndWait(Command.GET_LATEST_FROM_CACHE);
        assertNotNull(computingThread);
        reader.performAndWait(Command.UNLOCK_AFTER_READ); // this shall unblock the writer
        writer.performAndWait(Command.UNLOCK_AFTER_WRITE);
        assertTrue(System.currentTimeMillis()-start < 10000); // must not take longer than 5s, otherwise a locking conflict must have occurred;
        // see also LockUtil.NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK
    }
    
    @After
    public void tearDown() throws InterruptedException {
        reader.perform(Command.EXIT);
        reader.waitUntilRunning();
        writer.perform(Command.EXIT);
        writer.waitUntilRunning();
        readerThread.join();
        writerThread.join();
        assertFalse(reader.isRunning());
        assertFalse(writer.isRunning());
        assertTrue(lock.getReaders().isEmpty());
        assertNull(lock.getWriter());
    }
    
    private enum Command { EXIT, LOCK_FOR_READ, LOCK_FOR_WRITE, UNLOCK_AFTER_READ, UNLOCK_AFTER_WRITE, TRIGGER_CACHE_UPDATE,
        GET_FROM_CACHE, GET_LATEST_FROM_CACHE }

    private static class LockingScript implements Runnable {
        private static final Logger logger = Logger.getLogger(LockingScript.class.getName());
        private boolean running;
        private boolean waitingForLock;
        private final SmartFutureCache<String, String, EmptyUpdateInterval> sfc;
        private final NamedReentrantReadWriteLock lock;
        private final BlockingQueue<Command> commandQueue;

        public LockingScript(NamedReentrantReadWriteLock lock, SmartFutureCache<String, String, EmptyUpdateInterval> sfc) {
            this.lock = lock;
            this.commandQueue = new LinkedBlockingQueue<Command>();
            this.sfc = sfc;
        }
        
        public void waitUntilWaitingForLock() throws InterruptedException {
            synchronized (this) {
                while (!waitingForLock) {
                    wait();
                }
            }
            Thread.sleep(10); // sleep a little to make it incredibly likely that the LockUtil.lockFor...(...) method now really acquires the lock
        }

        public void perform(Command command) {
            commandQueue.offer(command);
        }
        
        public void performAndWait(Command command) throws InterruptedException {
            commandQueue.offer(command);
            synchronized (commandQueue) {
                while (!commandQueue.isEmpty()) {
                    commandQueue.wait();
                }
            }
        }
        
        public boolean isRunning() {
            return running;
        }
        
        public void waitUntilRunning() throws InterruptedException {
            synchronized (this) {
                while (!running) {
                    this.wait();
                }
            }
        }

        @Override
        public void run() {
            synchronized (this) {
                running = true;
                this.notifyAll();
            }
            Command command;
            try {
                while ((command=commandQueue.take()) != Command.EXIT) {
                    logger.info("Took command "+command.name()+" in thread "+Thread.currentThread().getName());
                    switch (command) {
                    case LOCK_FOR_READ:
                        synchronized (this) {
                            waitingForLock = true;
                            notifyAll(); // as good as it gets; the lock statement is still outside the synchronized block
                        }
                        LockUtil.lockForRead(lock);
                        waitingForLock = false;
                        break;
                    case LOCK_FOR_WRITE:
                        synchronized (this) {
                            waitingForLock = true;
                            notifyAll(); // as good as it gets; the lock statement is still outside the synchronized block
                        }
                        LockUtil.lockForWrite(lock);
                        waitingForLock = false;
                        break;
                    case UNLOCK_AFTER_READ:
                        LockUtil.unlockAfterRead(lock);
                        break;
                    case UNLOCK_AFTER_WRITE:
                        LockUtil.unlockAfterWrite(lock);
                        break;
                    case EXIT:
                        throw new RuntimeException("We excluded this in the while condition; what's going on here...?");
                    case GET_FROM_CACHE:
                        sfc.get(CACHE_KEY, /* waitForLatest */ false);
                        break;
                    case GET_LATEST_FROM_CACHE:
                        sfc.get(CACHE_KEY, /* waitForLatest */ true);
                        break;
                    case TRIGGER_CACHE_UPDATE:
                        sfc.triggerUpdate(CACHE_KEY, /* updateInterval */ null);
                        break;
                    }
                    logger.info("Done processing command "+command.name()+" in thread "+Thread.currentThread().getName());
                    synchronized (commandQueue) {
                        commandQueue.notifyAll(); // unblock wait in performAndWait
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Someone interrupted us", e);
            } finally {
                running = false;
            }
        }
    }

}
