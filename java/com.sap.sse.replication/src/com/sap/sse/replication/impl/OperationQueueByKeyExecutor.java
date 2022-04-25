package com.sap.sse.replication.impl;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class OperationQueueByKeyExecutor {
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

    public void schedule(Object keyForAsynchronousExection, Runnable runnable) {
        final Object monitorForKey;
        final Deque<Runnable> queueForKey;
        synchronized (this) {
            queueForKey = operationQueuesByKey.computeIfAbsent(keyForAsynchronousExection, key->new ConcurrentLinkedDeque<>());
            if (queueForKey.isEmpty()) {
                monitorForKey = new Object();
                monitorsPerKey.put(keyForAsynchronousExection, monitorForKey);
            } else {
                monitorForKey = monitorsPerKey.get(keyForAsynchronousExection);
            }
        }
        synchronized (monitorForKey) {
            if (queueForKey.isEmpty()) {
                // the task can be scheduled already, even though the queue is still empty,
                // because we're under the key's monitor, and all reading from the key-specific
                // queue must happen under that same monitor
                executor.execute(()->workOnQueueForKey(keyForAsynchronousExection));
            }
            queueForKey.add(runnable);
        }
    }

    private void workOnQueueForKey(Object keyForAsynchronousExection) {
        boolean finished = false;
        while (!finished) {
            final Runnable nextOperation;
            synchronized (monitorsPerKey.get(keyForAsynchronousExection)) {
                final Deque<Runnable> queueForKey = operationQueuesByKey.get(keyForAsynchronousExection);
                nextOperation = queueForKey.pollFirst();
                if (queueForKey.isEmpty()) {
                    finished = true;
                    synchronized (this) {
                        monitorsPerKey.remove(keyForAsynchronousExection);
                        operationQueuesByKey.remove(keyForAsynchronousExection);
                    }
                }
            }
            nextOperation.run();
        }
    }
}
