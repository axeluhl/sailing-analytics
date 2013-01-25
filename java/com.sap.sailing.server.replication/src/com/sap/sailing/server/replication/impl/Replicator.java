package com.sap.sailing.server.replication.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

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
public class Replicator implements Runnable {
    private final static Logger logger = Logger.getLogger(Replicator.class.getName());
    
    private final ReplicationMasterDescriptor master;
    private final HasRacingEventService racingEventServiceTracker;
    private final List<RacingEventServiceOperation<?>> queue;
    private final QueueingConsumer consumer;
    
    /**
     * If the replicator is suspended, messages received are queued.
     */
    private boolean suspended;
    
    /**
     * Starts the replicator immediately, not holding back messages received but forwarding them directly.
     * 
     * @param master
     *            descriptor of the master server from which this replicator receives messages
     * @param racingEventServiceTracker
     *            OSGi service tracker for the replica to which to apply the messages received
     * @param consumer the RabbitMQ consumer from which to load messages
     */
    public Replicator(ReplicationMasterDescriptor master, HasRacingEventService racingEventServiceTracker, QueueingConsumer consumer) {
        this(master, racingEventServiceTracker, /* startSuspended */ false, consumer);
    }
    
    public Replicator(ReplicationMasterDescriptor master, HasRacingEventService racingEventServiceTracker, boolean startSuspended, QueueingConsumer consumer) {
        this.queue = new ArrayList<RacingEventServiceOperation<?>>();
        this.master = master;
        this.racingEventServiceTracker = racingEventServiceTracker;
        this.suspended = startSuspended;
        this.consumer = consumer;
    }
    
    /**
     * Starts fetching messages from the {@link #consumer}. After receiving a single message, assumes it's a serialized
     * {@link RacingEventServiceOperation}, and applies it to the {@link RacingEventService} which is obtained from the
     * service tracker passed to this replicator at construction time.
     */
    @Override
    public void run() {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        while (true) {
            try {
                Delivery delivery = consumer.nextDelivery();
                byte[] bytesFromMessage = delivery.getBody();
                // Set this object's class's class loader as context for de-serialization so that all exported classes
                // of all required bundles/packages can be deserialized at least
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                ObjectInputStream ois = DomainFactory.INSTANCE.createObjectInputStreamResolvingAgainstThisFactory(
                        new ByteArrayInputStream(bytesFromMessage));
                RacingEventServiceOperation<?> operation = (RacingEventServiceOperation<?>) ois.readObject();
                applyOrQueue(operation);
            } catch (ShutdownSignalException sse) {
                logger.info("Received "+sse.getMessage()+". Terminating "+this);
                break;
            } catch (Exception e) {
                logger.info("Exception while processing replica: "+e.getMessage());
                logger.throwing(Replicator.class.getName(), "run", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }
    
    public synchronized boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * If the replicator is currently {@link #suspended}, the <code>operation</code> is queued, otherwise immediately applied to
     * the receiving replica.
     */
    private synchronized void applyOrQueue(RacingEventServiceOperation<?> operation) {
        if (suspended) {
            queue(operation);
        } else {
            apply(operation);
        }
    }

    private synchronized void apply(RacingEventServiceOperation<?> operation) {
        racingEventServiceTracker.getRacingEventService().apply(operation);
    }
    
    private synchronized void queue(RacingEventServiceOperation<?> operation) {
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
        for (Iterator<RacingEventServiceOperation<?>> i=queue.iterator(); i.hasNext(); ) {
            RacingEventServiceOperation<?> operation = i.next();
            i.remove();
            apply(operation);
        }
        assert queue.isEmpty();
        notifyAll();
    }

    public synchronized boolean isSuspended() {
        return suspended;
    }

    @Override
    public String toString() {
        return "Replicator for master "+master+", queue size: "+queue.size();
    }

}
