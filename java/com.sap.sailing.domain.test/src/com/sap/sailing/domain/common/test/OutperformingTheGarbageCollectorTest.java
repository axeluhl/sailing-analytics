package com.sap.sailing.domain.common.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class OutperformingTheGarbageCollectorTest {
    private static final Logger logger = Logger.getLogger(OutperformingTheGarbageCollectorTest.class.getName());
    private static final int MAX_THREADPOOL_SIZE = 5;
    private static final int BATCH_SIZE = 100000;
    private static final int NUMBER_OF_OBJECTS_TO_CREATE_INITIALLY = 50000000;
    private static final int NUMBER_OF_FOLLOW_UP_OBJECTS_TO_CREATE = 100000000;
    private final Random random = new Random();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(MAX_THREADPOOL_SIZE, MAX_THREADPOOL_SIZE, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final ThreadLocal<HashMap<Integer, String>> mapTemporarilyHoldingGarbage = ThreadLocal.withInitial(HashMap::new);
    private int numberOfFollowUpTasksCreated;
    
    @Test
    public void testOutperformingTheGarbageCollector() throws InterruptedException {
        for (int i=0; i<NUMBER_OF_OBJECTS_TO_CREATE_INITIALLY/BATCH_SIZE; i++) {
            executor.execute(()->produceSomeContentAndRemoveSomeOtherContent());
        }
        logger.info("Done with producing tasks; waiting for task queue to become empty.");
        while (!executor.getQueue().isEmpty()) {
            logger.info("Queue still has "+executor.getQueue().size()+" elements;  "+
                    numberOfFollowUpTasksCreated+" of "+NUMBER_OF_FOLLOW_UP_OBJECTS_TO_CREATE/BATCH_SIZE+
                    " follow-up tasks produced; waiting...");
            Thread.sleep(1000);
        }
        logger.info("Task queue empty; terminating");
    }
    
    /**
     * Produces {@link #BATCH_SIZE} map entries and then turns them into garbage again by removing them again from the map.
     * If follow-up tasks for less than {@link #NUMBER_OF_FOLLOW_UP_OBJECTS_TO_CREATE} have been created, enqueue another one.
     */
    private void produceSomeContentAndRemoveSomeOtherContent() {
        for (int i=0; i<BATCH_SIZE; i++) {
            mapTemporarilyHoldingGarbage.get().put(random.nextInt(Integer.MAX_VALUE), ""+random.nextDouble());
        }
        final Iterator<Entry<Integer, String>> i = mapTemporarilyHoldingGarbage.get().entrySet().iterator();
        int count = 0;
        try {
            while (i.hasNext() && count++ < BATCH_SIZE) {
                i.next();
                i.remove();
            }
        } catch (NoSuchElementException e) {
            // ignore because some other thread may already have removed the last element, and that's OK then
            logger.log(Level.INFO, "Very strange", e);
        }
        if (numberOfFollowUpTasksCreated < NUMBER_OF_FOLLOW_UP_OBJECTS_TO_CREATE/BATCH_SIZE) {
            numberOfFollowUpTasksCreated++;
            executor.execute(()->produceSomeContentAndRemoveSomeOtherContent());
        }
    }
}
