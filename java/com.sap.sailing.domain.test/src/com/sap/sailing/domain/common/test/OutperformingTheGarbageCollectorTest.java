package com.sap.sailing.domain.common.test;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.Test;

public class OutperformingTheGarbageCollectorTest {
    private static final Logger logger = Logger.getLogger(OutperformingTheGarbageCollectorTest.class.getName());
    private static final int MAX_THREADPOOL_SIZE = 300;
    private static final int NUMBER_OF_OBJECTS_TO_CREATE = 50000000;
    private final Random random = new Random();
    private final Executor executor = new ThreadPoolExecutor(MAX_THREADPOOL_SIZE, MAX_THREADPOOL_SIZE, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final ConcurrentHashMap<Integer, String> mapTemporarilyHoldingGarbage = new ConcurrentHashMap<>();
    
    @Test
    public void testOutperformingTheGarbageCollector() throws InterruptedException {
        for (int i=0; i<NUMBER_OF_OBJECTS_TO_CREATE; i++) {
            executor.execute(()->produceSomeContentAndRemoveSomeOtherContent());
        }
        logger.info("Done with producing tasks");
    }
    
    private void produceSomeContentAndRemoveSomeOtherContent() {
        mapTemporarilyHoldingGarbage.put(random.nextInt(Integer.MAX_VALUE), ""+random.nextDouble());
        final Iterator<Entry<Integer, String>> i = mapTemporarilyHoldingGarbage.entrySet().iterator();
        try {
            if (i.hasNext()) {
                i.next();
                i.remove();
            } else {
                logger.info("Strange; the collection is empty although we just added an element; welcome to concurrency...");
            }
        } catch (NoSuchElementException e) {
            // ignore because some other thread may already have removed the last element, and that's OK then
        }
    }
}
