package com.sap.sse.replication.impl;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationMasterDescriptor;

/**
 * Receives {@link RacingEventServiceOperation}s through JMS and
 * {@link RacingEventService#apply(RacingEventServiceOperation) applies} them to the {@link RacingEventService} passed
 * to this replicator at construction. When started in suspended mode, messages received will be turned into
 * {@link RacingEventServiceOperation}s and then queued until {@link #setSuspended(boolean) setSuspended(false)} is invoked
 * which applies all queued operations before applying the ones received later.<p>
 * 
 * The receiver takes care of synchronizing receiving, suspending/resuming and queuing. Waiters are notified
 * whenever the result of {@link #isQueueEmpty} changes.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Replicator<S, O extends OperationWithResult<S, ?>> implements Runnable {
    private final static Logger logger = Logger.getLogger(Replicator.class.getName());
    
    private static final long CHECK_INTERVAL_MILLIS = 2000; // how long (milliseconds) to pause before checking connection again
    private static final int CHECK_COUNT = 150; // how long to check, value is CHECK_INTERVAL second steps
    
    private final ReplicationMasterDescriptor master;
    private final HasReplicable<S, O> replicableProvider;
    private final List<OperationWithResult<S, ?>> queue;
    
    private QueueingConsumer consumer;
    
    /**
     * How many checks have been performed due to a failing connection?
     */
    private int checksPerformed = 0;
    
    /**
     * If the replicator is suspended, messages received are queued.
     */
    private boolean suspended;
    
    private boolean stopped = false;
    
    /**
     * When many updates are triggered in a short period of time by a single thread, ensure that the single thread
     * providing the updates is not outperformed by all the re-calculations happening here. Leave at least one
     * core to other things, but by using at least three threads ensure that no simplistic deadlocks may occur.
     */
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors()-1, 3);

    /**
     * Used for the parallel execution of operations that don't
     * {@link RacingEventServiceOperation#requiresSynchronousExecution()}.
     */
    private final static Executor executor = new ThreadPoolExecutor(/* corePoolSize */ THREAD_POOL_SIZE,
            /* maximumPoolSize */ THREAD_POOL_SIZE,
            /* keepAliveTime */ 60, TimeUnit.SECONDS,
            /* workQueue */ new LinkedBlockingQueue<Runnable>());

    /**
     * @param master
     *            descriptor of the master server from which this replicator receives messages
     * @param replicableProvider
     *            OSGi service tracker for the replica to which to apply the messages received
     * @param startSuspended
     *            decides whether to stars the replicator immediately, not holding back messages received but forwarding
     *            them directly.
     * @param consumer
     *            the RabbitMQ consumer from which to load messages
     */
    public Replicator(ReplicationMasterDescriptor master, HasReplicable<S, O> replicableProvider, boolean startSuspended, QueueingConsumer consumer) {
        this.queue = new ArrayList<OperationWithResult<S, ?>>();
        this.master = master;
        this.replicableProvider = replicableProvider;
        this.suspended = startSuspended;
        this.consumer = consumer;
    }
    
    /**
     * Starts fetching messages from the {@link #consumer}. After receiving a single message, assumes it's an
     * {@link Iterable} of serialized {@link RacingEventServiceOperation} objects, and applies it to the
     * {@link RacingEventService} which is obtained from the service tracker passed to this replicator at construction
     * time.
     * 
     * @see ReplicationServiceImpl#executed(RacingEventServiceOperation)
     */
    @Override
    public void run() {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        long messageCount = 0;
        long operationCount = 0;
        Field _queue;
        try {
            _queue = QueueingConsumer.class.getDeclaredField("_queue");
            _queue.setAccessible(true);
        } catch (Exception e) {
            _queue = null;
        }
        final boolean logsFine = logger.isLoggable(Level.FINE);
        while (!isBeingStopped()) {
           try {
                Delivery delivery = consumer.nextDelivery();
                messageCount++;
                if (_queue != null) {
                    if (logsFine || messageCount % 10l == 0) {
                        try {
                            logger.log(messageCount%10l==0 ? Level.INFO : Level.FINE,
                                    "Inbound replication queue size: "+((BlockingQueue<?>) _queue.get(consumer)).size());
                        } catch (Exception e) {
                            // it didn't work; but it's a log message only...
                            logger.info("Received another 10000 replication messages");
                        }
                    }
                }
                byte[] bytesFromMessage = delivery.getBody();
                checksPerformed = 0;
                // Set this object's class's class loader as context for de-serialization so that all exported classes
                // of all required bundles/packages can be deserialized at least
                Replicable<S, O> replicable = replicableProvider.getReplicable();
                Thread.currentThread().setContextClassLoader(replicable.getClass().getClassLoader());
                ObjectInputStream ois = replicable
                        .createObjectInputStreamResolvingAgainstCache(
                                new GZIPInputStream(new ByteArrayInputStream(bytesFromMessage)));
                int operationsInMessage = 0;
                try {
                    while (true) {
                        byte[] serializedOperation = (byte[]) ois.readObject();
                        ObjectInputStream operationOIS = replicable
                                .createObjectInputStreamResolvingAgainstCache(new ByteArrayInputStream(serializedOperation));
                        @SuppressWarnings("unchecked")
                        OperationWithResult<S, ?> operation = (OperationWithResult<S, ?>) operationOIS.readObject();
                        operationCount++;
                        operationsInMessage++;
                        if (operationCount % 10000l == 0) {
                            logger.info("Received " + operationCount + " operations so far");
                        }
                        applyOrQueue(operation);
                    }
                } catch (EOFException eof) {
                    logger.fine("Reached EOF on replication message after having read "+operationsInMessage+" operations");
                    // reached EOF; expected
                }
            } catch (ConsumerCancelledException cce) {
                logger.info("Consumer has been shut down properly.");
                break;
            } catch (InterruptedException irr) {
                logger.info("Application requested shutdown.");
                break;
            } catch (ShutdownSignalException sse) {
                /* make sure to respond to a stop event without waiting */
                if (isBeingStopped()) {
                    break;
                }
                if (sse.isInitiatedByApplication()) {
                    logger.severe("Application shut down messaging queue for " + this.toString());
                    break;
                }
                logger.info(sse.getMessage());
                if (checksPerformed <= CHECK_COUNT) {
                    try {
                        Thread.sleep(CHECK_INTERVAL_MILLIS);
                        
                        /* isOpen() will return false if the channel has been closed. This
                         * does not hold when the connection is dropped.
                         */
                        if (!this.consumer.getChannel().isOpen()) {
                            /* for a reconnection we need to instantiate a new consumer */
                            try {
                                logger.info("Channel seems to be closed. Trying to reconnect consumer queue...");
                                this.consumer = master.getConsumer();
                                logger.info("OK - channel reconnected!");
                                Thread.sleep(CHECK_INTERVAL_MILLIS);
                                checksPerformed += 1;
                            } catch (IOException eio) {
                                // do not print exceptions known to occur
                            }
                        }
                    } catch (InterruptedException eir) {
                        eir.printStackTrace();
                    }
                    checksPerformed += 1;
                    continue;
                } else {
                    logger.severe("Grace time (" + CHECK_COUNT*(CHECK_INTERVAL_MILLIS/1000) + "secs) is over. Terminating replication listener " + this.toString());
                    // XXX: Also make sure that all handlers get notifications about this
                    break;
                }
            } catch (Exception e) {
                logger.info("Exception while processing replica: "+e.getMessage());
                logger.log(Level.SEVERE, "run", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        logger.info("Stopped replicator thread. This server will no longer receive events from a master.");
    }
    
    public synchronized boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * If the replicator is currently {@link #suspended}, the <code>operation</code> is queued, otherwise immediately applied to
     * the receiving replica.
     */
    private synchronized void applyOrQueue(OperationWithResult<S, ?> operation) {
        if (suspended) {
            queue(operation);
        } else {
            apply(operation);
        }
    }

    private synchronized void apply(final OperationWithResult<S, ?> operation) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                replicableProvider.getReplicable().apply(operation);
            }
        };
        if (operation.requiresSynchronousExecution()) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }
    
    private synchronized void queue(OperationWithResult<S, ?> operation) {
        if (queue.isEmpty()) {
            notifyAll();
        }
        queue.add(operation);
        assert !queue.isEmpty();
    }
    
    public synchronized void setSuspended(final boolean suspended) {
        if (this.suspended != suspended) {
            this.suspended = suspended;
            if (!this.suspended) {
                applyQueue();
            }
        }
    }
    
    private synchronized void applyQueue() {
        for (Iterator<OperationWithResult<S, ?>> i=queue.iterator(); i.hasNext(); ) {
            OperationWithResult<S, ?> operation = i.next();
            i.remove();
            try {
                apply(operation);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error applying queued, replicated operation "+operation+". Continuing with next queued operation.", e);
            }
        }
        assert queue.isEmpty();
        notifyAll();
    }

    public synchronized boolean isSuspended() {
        return suspended;
    }
    
    public synchronized void stop() {
        if (isSuspended()) {
            /* make sure to apply everything in queue before stopping this thread */
            applyQueue();
        }
        logger.info("Signaled Replicator thread to stop asap.");
        stopped = true;
        master.stopConnection();
    }
    
    public synchronized boolean isBeingStopped() {
        return stopped;
    }

    @Override
    public String toString() {
        return "Replicator for master "+master+", queue size: "+queue.size();
    }

}
