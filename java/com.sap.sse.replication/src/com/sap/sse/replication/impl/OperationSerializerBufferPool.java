package com.sap.sse.replication.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * A pool of {@link OperationSerializerBufferImpl} objects, combined with a {@link ThreadPoolExecutor} whose size matches
 * that of the number of pooled {@link OperationSerializerBufferImpl} objects. Such a buffer pool can be used to parallelize
 * the rather expensive serialization of {@link Operation} objects for those operations that don't
 * {@link Operation#requiresSynchronousExecution() require synchronous execution}. Those operations that occur in great
 * numbers, such as the ones for inserting fixes into time-sequenced tracks, can be expected to allow for this type
 * of asynchronous processing as their time stamps support out-of-order processing.<p>
 * 
 * Each thread in the pool is linked to a single {@link OperationSerializerBufferImpl}. The entire pool has a single inbound
 * {@link BlockingDeque} allowing multiple threads to add operation/replicable pairs to it. Each thread tries to
 * {@link BlockingDeque#take() take} such a pair from the queue and hand it to its {@link OperationSerializerBufferImpl}'s
 * {@link OperationSerializerBufferImpl#write(com.sap.sse.replication.OperationWithResult, com.sap.sse.replication.Replicable) write}
 * method.<p>
 * 
 * The pool is created in "stopped" mode and needs to be started by calling {@link #start()} before submitting operations
 * to it with the {@link #write(OperationWithResult, Replicable)} method. Conversely, the pool can be stopped using the
 * {@link #stop()} method which tries to free up any resources, in particular the thread pool and its threads. Once stopped,
 * the pool must be {@link #start()}ed again before submitting operations again.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class OperationSerializerBufferPool implements OperationSerializerBuffer {
    private static final Logger logger = Logger.getLogger(OperationSerializerBufferPool.Runner.class.getName());

    private ScheduledExecutorService executor;
    
    private final int size;
    
    private final BlockingDeque<Pair<OperationWithResult<?, ?>, Replicable<?, ?>>> queue;

    private final ReplicationMessageSender sender;

    private final Duration timeout;

    private final int maximumBufferSizeInBytes;

    private final Timer timer;
    
    private static final Pair<OperationWithResult<?, ?>, Replicable<?, ?>> STOP = new Pair<>(null, null);
    
    private class Runner implements Runnable {
        private final OperationSerializerBufferImpl buffer;
        
        private Runner(final ReplicationMessageSender sender, final Duration timeout,
                final int maximumBufferSizeInBytes, final Timer timer) throws IOException {
            buffer = new OperationSerializerBufferImpl(sender, timeout, maximumBufferSizeInBytes, timer);
        }
        
        @Override
        public void run() {
            boolean interrupted = false;
            while (!interrupted) {
                Pair<OperationWithResult<?, ?>, Replicable<?, ?>> operationAndReplicable;
                try {
                    operationAndReplicable = queue.take();
                    if (operationAndReplicable == STOP) {
                        logger.fine("Read STOP; terminating one operation serializer buffer pool thread");
                        interrupted = true;
                    } else {
                        try {
                            buffer.write(operationAndReplicable.getA(), operationAndReplicable.getB());
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Couldn't write operation for replicable "+operationAndReplicable, e);
                        }
                    }
                } catch (InterruptedException e1) {
                    logger.warning("Interrupted; terminating...");
                    interrupted = true;
                }
            }
        }
    }
    
    public OperationSerializerBufferPool(final ReplicationMessageSender sender, final Duration timeout,
            final int maximumBufferSizeInBytes, final Timer timer) throws IOException {
        size = ThreadPoolUtil.INSTANCE.getReasonableThreadPoolSize();
        queue = new LinkedBlockingDeque<>();
        logger.info("Creating operation serializer buffer pool for outbound replication of size "+size);
        this.sender = sender;
        this.timeout = timeout;
        this.maximumBufferSizeInBytes = maximumBufferSizeInBytes;
        this.timer = timer;
    }

    public synchronized void start() throws IOException {
        if (executor == null) {
            executor = ThreadPoolUtil.INSTANCE.createForegroundTaskThreadPoolExecutor(size, "OperationSerializerBufferPool thread pool");
            for (int i=0; i<size; i++) {
                executor.execute(new Runner(sender, timeout, maximumBufferSizeInBytes, timer));
            }
        }
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>> void write(OperationWithResult<?, ?> operation, Replicable<S, O> replicable) throws IOException {
        queue.add(new Pair<>(operation, replicable));
    }
    
    /**
     * Informs all threads in this pool to shut down cleanly.
     */
    public synchronized void stop() {
        if (executor != null) {
            logger.info("Stopping operation serializer buffer pool");
            executor.shutdown();
            executor = null;
            for (int i=0; i<size; i++) {
                queue.add(STOP);
            }
        }
    }
}
