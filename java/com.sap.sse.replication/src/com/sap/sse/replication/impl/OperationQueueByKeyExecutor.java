package com.sap.sse.replication.impl;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.sap.sse.common.TimePoint;
import com.sap.sse.operationaltransformation.Operation;

/**
 * Collaborates with {@link ReplicationReceiverImpl} to allow parallel execution of operations, but only for those
 * that have different {@link Operation#getKeyForAsynchronousExecution() keys for asynchronous execution}. The receiver
 * {@link #schedule(Object, Runnable) schedules} operations (wrapped as {@link Runnable}s) with an object of this type
 * which puts it into a {@link Deque double-ended queue} specific to the key for asynchronous execution as provided by
 * the operation. If it is the first operation in a new {@link Deque} then a task is scheduled for immediate execution
 * which continues to poll the {@link Deque} and apply its operations until drained. This way, if the replication
 * received continues to receive operations with the same key while the first operation with that key is being applied,
 * the next operations with an equal key will be enqueued in the same queue and will be picked up by the already running
 * task that is specific to the key. When the queue is drained, the task terminates and the queue is removed from
 * {@link #operationQueuesByKey}.<p>
 * 
 * With this approach, only one task per equal key is scheduled with the {@link Executor} passed to the constructor,
 * ensuring that operations with equal keys don't block each other during parallel execution.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class OperationQueueByKeyExecutor {
    private static final Logger logger = Logger.getLogger(OperationQueueByKeyExecutor.class.getName());

    private final Executor executor;
    
    /**
     * Holds double-ended queues for each key for which a task has been {@link #schedule(Object, Runnable) scheduled}
     * with the {@link #executor} and which hasn't completed yet.
     * <p>
     * 
     * Changing this map must happen exclusively, while {@code synchronized} on this {@link OperationQueueByKeyExecutor}
     * object. Changing any of the value queues must happen while being synchronized on the corresponding
     * {@link #monitorsPerKey} value for an equal key.
     */
    private final ConcurrentMap<Object, Deque<Runnable>> operationQueuesByKey;
    
    /**
     * For any single-key operation, such as looking up or modifying its queue, a monitor object must be obtained (and
     * created if it does not yet exist) from this map for the respective key and must be {@code synchronized} on. The
     * mapping can be removed if and only if the corresponding mapping is removed from {@link #operationQueuesByKey}.
     * <p>
     * 
     * Changing this map must happen exclusively, while {@code synchronized} on this {@link OperationQueueByKeyExecutor}
     * object.
     */
    private final ConcurrentMap<Object, Object> monitorsPerKey;

    OperationQueueByKeyExecutor(Executor executor) {
        super();
        this.executor = executor;
        this.operationQueuesByKey = new ConcurrentHashMap<>();
        this.monitorsPerKey = new ConcurrentHashMap<>();
    }

    public void schedule(Object keyForAsynchronousExecution, Runnable runnable) {
        final Object monitorForKey;
        final Deque<Runnable> queueForKey;
        final boolean queueCreated;
        synchronized (this) {
            if (operationQueuesByKey.containsKey(keyForAsynchronousExecution)) {
                queueForKey = operationQueuesByKey.get(keyForAsynchronousExecution);
                monitorForKey = monitorsPerKey.get(keyForAsynchronousExecution);
                queueCreated = false;
            } else {
                logger.fine(()->"Creating operation queue for key "+keyForAsynchronousExecution);
                queueForKey = new ConcurrentLinkedDeque<>();
                queueCreated = true;
                operationQueuesByKey.put(keyForAsynchronousExecution, queueForKey);
                monitorForKey = new Object();
                monitorsPerKey.put(keyForAsynchronousExecution, monitorForKey);
            }
        }
        synchronized (monitorForKey) {
            if (queueCreated) {
                // the task can be scheduled already, even though the queue is still empty,
                // because we're under the key's monitor, and all reading from the key-specific
                // queue must happen under that same monitor
                executor.execute(()->workOnQueueForKey(keyForAsynchronousExecution));
            }
            queueForKey.add(runnable);
        }
    }

    /**
     * Removes operations from the queue for the {@code keyForAsynchronousExecution} which can be expected to not be
     * empty when this method is called. Changes to the queue are done while {@code synchronized} on the
     * {@link #monitorsPerKey monitor for the key}, in particular removing an element and deciding whether this
     * task can end because the queue is empty and hence removing the queue and the monitor consistently.
     */
    private void workOnQueueForKey(Object keyForAsynchronousExecution) {
        final Deque<Runnable> queueForKey;
        final int[] count = { 1 };
        final TimePoint[] started = new TimePoint[1];
        logger.fine(()->{
            started[0] = TimePoint.now();
            return "Started work on queue for key "+keyForAsynchronousExecution;
        });
        Runnable nextOperation;
        synchronized (monitorsPerKey.get(keyForAsynchronousExecution)) {
            queueForKey = operationQueuesByKey.get(keyForAsynchronousExecution);
            nextOperation = queueForKey.pollFirst();
        }
        do {
            nextOperation.run();
            synchronized (monitorsPerKey.get(keyForAsynchronousExecution)) {
                if (queueForKey.isEmpty()) {
                    nextOperation = null;
                    synchronized (this) {
                        monitorsPerKey.remove(keyForAsynchronousExecution);
                        operationQueuesByKey.remove(keyForAsynchronousExecution);
                    }
                } else {
                    nextOperation = queueForKey.pollFirst();
                    count[0]++;
                }
            }
        } while (nextOperation != null);
        logger.fine(()->"Terminating tasks for operation queue for key "+keyForAsynchronousExecution+" after running "+count[0]+" operations "+
                "which took "+started[0].until(TimePoint.now()));
    }
}
