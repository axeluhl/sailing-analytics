package com.sap.sse.replication.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicablesProvider;
import com.sap.sse.replication.ReplicationMasterDescriptor;

/**
 * Receives {@link OperationWithResult} objects from a message queue and {@link Replicable#apply(OperationWithResult)
 * applies} them to the {@link Replicable} objects that can be found through the {@link ReplicablesProvider} passed to
 * this replicator at construction, based on the {@link Replicable#getId() replicable's ID} which is used in the stream
 * to prefix the operations.
 * <p>
 * 
 * The corresponding writer for this protocol is implemented by
 * {@link ReplicationServiceImpl#broadcastOperation(OperationWithResult, Replicable)}.<p>
 * 
 * When started in suspended mode, messages received will be turned into {@link OperationWithResult} objects and then
 * queued until {@link #setSuspended(boolean) setSuspended(false)} is invoked which applies all queued operations before
 * applying the ones received later.
 * <p>
 * 
 * The receiver takes care of synchronizing receiving, suspending/resuming and queuing. Waiters are notified whenever
 * the result of {@link #isQueueEmpty} changes.<p>
 * 
 * Clients can {@link Object#wait()} on this object and will be {@link Object#notify() notified} when one of the queues
 * for one {@link Replicable} has been consumed so that it is empty. As new operations may arrive at any time, this is
 * no guarantee for the queue remaining empty; however, it is a possible way to get informed about this interesting change
 * in state, particularly in case it was really the last operation that was received.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Replicator implements Runnable {
    private final static Logger logger = Logger.getLogger(Replicator.class.getName());
    
    private static final long CHECK_INTERVAL_MILLIS = 2000; // how long (milliseconds) to pause before checking connection again
    private static final int CHECK_COUNT = 150; // how long to check, value is CHECK_INTERVAL second steps
    
    private final ReplicationMasterDescriptor master;
    private final ReplicablesProvider replicableProvider;
    
    /**
     * Keys are the {@link Replicable}s' IDs as string; values are the operation queues for the replicable identified by the
     * key.
     */
    private final Map<String, List<Pair<String, OperationWithResult<?, ?>>>> queueByReplicableIdAsString;
    
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
     * If permitted by the security manager, this is the <code>_queue</code> field accessor for the {@link QueueingConsumer}
     * class, enabling an inspection of the message queueing system's queue size of unprocessed messages.
     */
    private Field _queue;

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
    public Replicator(ReplicationMasterDescriptor master, ReplicablesProvider replicableProvider, boolean startSuspended, QueueingConsumer consumer) {
        this.queueByReplicableIdAsString = new HashMap<>();
        this.master = master;
        this.replicableProvider = replicableProvider;
        this.suspended = startSuspended;
        this.consumer = consumer;
        try {
            _queue = QueueingConsumer.class.getDeclaredField("_queue");
            _queue.setAccessible(true);
        } catch (Exception e) {
            _queue = null;
        }
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
        final boolean logsFine = logger.isLoggable(Level.FINE);
        while (!isBeingStopped()) {
           try {
                Delivery delivery = consumer.nextDelivery();
                messageCount++;
                if (_queue != null) {
                    if (logsFine || messageCount % 10l == 0) {
                        try {
                            logger.log(messageCount%10l==0 ? Level.INFO : Level.FINE,
                                    "Received "+messageCount+" replication messages with "+operationCount+" operations in total. Inbound replication queue size: "+getMessageQueueSize());
                        } catch (Exception e) {
                            // it didn't work; but it's a log message only...
                            logger.info("Received "+messageCount+" replication messages with "+operationCount+" operations in total.");
                        }
                    }
                }
                byte[] bytesFromMessage = delivery.getBody();
                checksPerformed = 0;
                // Set the replicable's class's class loader as context for deserialization so that all exported classes
                // of all required bundles/packages can be deserialized at least
                final GZIPInputStream uncompressedInputStream = new GZIPInputStream(new ByteArrayInputStream(bytesFromMessage));
                String replicableIdAsString = new DataInputStream(uncompressedInputStream).readUTF();
                Replicable<?, ?> replicable = replicableProvider.getReplicable(replicableIdAsString, /* wait */ false);
                if (replicable != null) {
                    Thread.currentThread().setContextClassLoader(replicable.getClass().getClassLoader());
                    ObjectInputStream ois = new ObjectInputStream(uncompressedInputStream); // no special stream required; only reading a generic byte[]
                    int operationsInMessage = 0;
                    try {
                        while (true) {
                            byte[] serializedOperation = (byte[]) ois.readObject();
                            readOperationAndApplyOrQueueIt(replicable, serializedOperation);
                            operationCount++;
                            operationsInMessage++;
                            if (operationCount % 10000l == 0) {
                                logger.info("Received " + operationCount + " operations so far");
                            }
                        }
                    } catch (EOFException eof) {
                        logger.fine("Reached EOF on replication message after having read " + operationsInMessage
                                + " operations");
                        // reached EOF; expected
                    }
                } else {
                    // otherwise, we don't know the replicable and simply drop the message with all the operations for the unknown recipient
                    Stream<Named> sn = StreamSupport.stream(replicableProvider.getReplicables().spliterator(), /* parallel */ false)
                        .map(r->(() -> r.getId().toString()));
                    logger.warning("received replication message for replicable with ID "+replicableIdAsString+" which is unknnown by this replicator "+
                            "which only knows "+
                            Util.join(", ", sn::iterator));
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

    /**
     * @return the number of unprocessed messages in the inbound message queue
     */
    private int getMessageQueueSize() throws IllegalAccessException {
        return getInboundMessageQueue().size();
    }

    /**
     * @return the message queueing system's message queue from which this replicator reads messages; can be used
     * to check if the queue is empty or to determine the number of elements in the queue
     */
    private BlockingQueue<?> getInboundMessageQueue() throws IllegalAccessException {
        return (BlockingQueue<?>) _queue.get(consumer);
    }
    
    private <S, O extends OperationWithResult<S, ?>> void readOperationAndApplyOrQueueIt(Replicable<S, O> replicable,
            byte[] serializedOperation) throws ClassNotFoundException, IOException {
        OperationWithResult<S, ?> operation = replicable.readOperation(new ByteArrayInputStream(serializedOperation));
        applyOrQueue(operation, replicable);
    }

    /**
     * If the replicator is currently {@link #suspended}, the <code>operation</code> is queued, otherwise immediately
     * applied to the receiving replica.
     * 
     * @param replicable
     *            the replicable to which to apply or for which to queue the operation
     */
    private synchronized <S, O extends OperationWithResult<S, ?>> void applyOrQueue(OperationWithResult<S, ?> operation, Replicable<S, O> replicable) {
        if (suspended) {
            queue(operation, replicable);
        } else {
            apply(operation, replicable);
        }
    }

    private synchronized <S, O extends OperationWithResult<S, ?>> void apply(final OperationWithResult<S, ?> operation, String replicableIdAsString) {
        @SuppressWarnings("unchecked")
        Replicable<S, O> replicable = (Replicable<S, O>) replicableProvider.getReplicable(replicableIdAsString, /* wait */ false);
        apply(operation, replicable);
    }
    
    private synchronized <S, O extends OperationWithResult<S, ?>> void apply(final OperationWithResult<S, ?> operation, Replicable<S, O> replicable) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                replicable.apply(operation);
            }
        };
        if (operation.requiresSynchronousExecution()) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }
    
    private synchronized void queue(OperationWithResult<?, ?> operation, Replicable<?, ?> replicable) {
        List<Pair<String, OperationWithResult<?, ?>>> queue = queueByReplicableIdAsString.get(replicable.getId().toString());
        if (queue.isEmpty()) {
            notifyAll();
        }
        queue.add(new Pair<String, OperationWithResult<?, ?>>(replicable.getId().toString(), operation));
        assert !queue.isEmpty();
    }
    
    public synchronized void setSuspended(final boolean suspended) {
        if (this.suspended != suspended) {
            this.suspended = suspended;
            if (!this.suspended) {
                applyQueues();
            }
        }
    }
    
    private synchronized void applyQueues() {
        for (Entry<String, List<Pair<String, OperationWithResult<?, ?>>>> r : queueByReplicableIdAsString.entrySet()) {
            final List<Pair<String, OperationWithResult<?, ?>>> queue = r.getValue();
            for (Iterator<Pair<String, OperationWithResult<?, ?>>> i = queue.iterator(); i.hasNext();) {
                Pair<String, OperationWithResult<?, ?>> replicableIdAsStringAndOperation = i.next();
                i.remove();
                try {
                    apply(replicableIdAsStringAndOperation.getB(), replicableIdAsStringAndOperation.getA());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error applying queued, replicated operation "
                            + replicableIdAsStringAndOperation + ". Continuing with next queued operation.", e);
                }
            }
            assert queue.isEmpty();
        }
        notifyAll();
    }

    public synchronized boolean isSuspended() {
        return suspended;
    }
    
    public synchronized void stop() {
        if (isSuspended()) {
            /* make sure to apply everything in queue before stopping this thread */
            applyQueues();
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
        long queueSize = queueByReplicableIdAsString.values().stream().mapToLong(l->l.size()).sum();
        return "Replicator for master "+master+", queue size: "+queueSize;
    }

    /**
     * @return <code>true</code> if all queues for all replicables are empty
     */
    public boolean isQueueEmpty() throws IllegalAccessException {
        return (_queue == null || getInboundMessageQueue().isEmpty()) && !queueByReplicableIdAsString.values().stream().anyMatch(q->!q.isEmpty());
    }

}
