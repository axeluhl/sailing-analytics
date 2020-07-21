package com.sap.sse.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sse.util.ThreadPoolUtil;

public class ThreadPoolWithThreadLocalsTest {

    private static final Logger logger = Logger.getLogger(ThreadPoolWithThreadLocalsTest.class.getName());

    private static Field threadLocalsField;
    private static Field inheritableThreadLocalsField;

    static {
        try {
            threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);

        } catch (NoSuchFieldException | SecurityException e) {
            logger.log(Level.SEVERE, "Problem finding field threadLocals", e);
        }
        try {
            inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocalsField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            logger.log(Level.SEVERE, "Problem finding field inheritableThreadLocals", e);
        }
    }

    @Test(expected = ThreadLocalCollisionException.class)
    public void testThreadReuseWithThreadLocalUsingFixedThreadPool() throws Exception {
        testThreadReuseWithThreadLocal(() -> Executors.newFixedThreadPool(2));
    }

    @Test
    public void testThreadReuseWithThreadLocalUsingNamedTracingScheduledThreadPoolExecutor() throws Exception {
        testThreadReuseWithThreadLocal(
                () -> ThreadPoolUtil.INSTANCE.createForegroundTaskThreadPoolExecutor("ThreadPoolWithThreadLocalsTest"));
    }

    @Test(expected = ThreadLocalCollisionException.class)
    public void testThreadReuseWithInheritableThreadLocalUsingFixedThreadPool() throws Exception {
        testThreadReuseWithInheritableThreadLocal(() -> Executors.newFixedThreadPool(2));
    }

    @Test
    public void testThreadReuseWithInheritableThreadLocalUsingNamedTracingScheduledThreadPoolExecutor()
            throws Exception {
        testThreadReuseWithInheritableThreadLocal(() -> ThreadPoolUtil.INSTANCE
                .createForegroundTaskThreadPoolExecutor("ThreadPoolWithInheritableThreadLocalsTest"));
    }

    private void testThreadReuseWithThreadLocal(final Supplier<ExecutorService> executorFactory) throws Exception {
        final ExecutorService executor = executorFactory.get();
        final Thread thread1 = executor.submit(() -> {
            ThreadLocal<String> tl = new ThreadLocal<>();
            tl.set("top secret");
            assertTrue(containsThreadLocalWithValue(Thread.currentThread(), "top secret", threadLocalsField));
            return Thread.currentThread();
        }).get();
        Thread thread2 = null;
        while (thread1 != thread2) {
            thread2 = executor.submit(() -> Thread.currentThread()).get();
        }
        assertEquals(thread1, thread2);
        if (containsThreadLocalWithValue(thread2, "top secret", threadLocalsField)) {
            throw new ThreadLocalCollisionException(
                    "Reused Thread contains some old top secret value from ThreadLocal!");
        }
    }

    private void testThreadReuseWithInheritableThreadLocal(final Supplier<ExecutorService> executorFactory)
            throws Exception {
        final ExecutorService executor = executorFactory.get();
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("top secret");
        final Thread thread1 = executor.submit(() -> {
            return Thread.currentThread();
        }).get();
        itl.remove();
        Thread thread2 = null;
        while (thread1 != thread2) {
            thread2 = executor.submit(() -> Thread.currentThread()).get();
        }
        assertEquals(thread1, thread2);
        if (containsThreadLocalWithValue(thread2, "top secret", inheritableThreadLocalsField)) {
            throw new ThreadLocalCollisionException(
                    "Reused Thread contains some old top secret value from InheritableThreadLocal!");
        }
    }

    private boolean containsThreadLocalWithValue(final Thread thread, final String value, final Field threadLocalsField)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Object threadLocals = threadLocalsField.get(thread);
        if (threadLocals != null) {
            final Field tableField = threadLocals.getClass().getDeclaredField("table");
            tableField.setAccessible(true);
            final Object[] table = (Object[]) tableField.get(threadLocals);
            for (int j = 0; j < table.length; j++) {
                final WeakReference<?> entry = (WeakReference<?>) table[j];
                if (entry != null) {
                    @SuppressWarnings("unchecked")
                    final ThreadLocal<Object> key = (ThreadLocal<Object>) entry.get();
                    if (key != null) {
                        final Field valueField = entry.getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        final Object entryvalue = valueField.get(entry);
                        if (value.equals(entryvalue)) {
                            logger.info(threadLocalsField.getName() + ": found thread local value \"" + value
                                    + "\" in thread " + thread.getId());
                            return true;
                        }
                    }
                }
            }
            logger.info("NOT found thread local value " + value + " in thread " + thread.getId());
            return false;
        } else {
            logger.info("NOT found any thread local in thread " + thread.getId());
            return false;
        }
    }

    private static class ThreadLocalCollisionException extends Exception {
        private static final long serialVersionUID = 5413856555774135094L;

        ThreadLocalCollisionException(String msg) {
            super(msg);
        }
    }
}
