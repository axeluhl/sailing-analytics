package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.util.impl.NamedTracingScheduledThreadPoolExecutor;

public class InheritableThreadLocalAndThreadPoolTest {
    private static InheritableThreadLocal<Integer> defaultThreadLocal;
    private static InheritableThreadLocal<Integer> threadLocalWithChildValueInit;
    private NamedTracingScheduledThreadPoolExecutor executor;
    
    @Before
    public void setUp() {
        defaultThreadLocal = new InheritableThreadLocal<>();
        threadLocalWithChildValueInit = new InheritableThreadLocal<Integer>() {
            @Override
            protected Integer childValue(Integer parentValue) {
                return 42;
            }
        };
        executor = new NamedTracingScheduledThreadPoolExecutor("Test Executor", /* corePoolSize */ 1);
    }
    
    @Test
    public void testSimpleValue() throws InterruptedException, ExecutionException {
        defaultThreadLocal.set(14);
        final Callable<Integer> reader1 = ()->defaultThreadLocal.get();
        final Future<Integer> future1 = executor.submit(reader1);
        defaultThreadLocal.set(15);
        final Callable<Integer> reader2 = ()->defaultThreadLocal.get();
        final Future<Integer> future2 = executor.submit(reader2);
        assertEquals(14, future1.get().intValue());
        assertEquals(15, future2.get().intValue());
    }

    @Test
    public void testMissingInitialization() throws InterruptedException, ExecutionException {
        threadLocalWithChildValueInit.set(123);
        final Callable<Integer> reader1 = ()->threadLocalWithChildValueInit.get();
        final Future<Integer> future1 = executor.submit(reader1);
        final Runnable setter = ()->threadLocalWithChildValueInit.set(43);
        executor.execute(setter);
        final Callable<Integer> reader2 = ()->threadLocalWithChildValueInit.get();
        final Future<Integer> future2 = executor.submit(reader2);
        assertEquals(42, future1.get().intValue());
        assertEquals(42, future2.get().intValue());
    }
}
