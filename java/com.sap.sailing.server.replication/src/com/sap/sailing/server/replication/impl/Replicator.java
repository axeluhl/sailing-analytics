package com.sap.sailing.server.replication.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

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
public class Replicator implements MessageListener {
    private final ReplicationMasterDescriptor master;
    private final HasRacingEventService racingEventServiceTracker;
    private final List<RacingEventServiceOperation<?>> queue;
    
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
     */
    public Replicator(ReplicationMasterDescriptor master, HasRacingEventService racingEventServiceTracker) {
        this(master, racingEventServiceTracker, /* startSuspended */ false);
    }
    
    public Replicator(ReplicationMasterDescriptor master, HasRacingEventService racingEventServiceTracker, boolean startSuspended) {
        this.queue = new ArrayList<RacingEventServiceOperation<?>>();
        this.master = master;
        this.racingEventServiceTracker = racingEventServiceTracker;
        this.suspended = startSuspended;
    }
    
    public synchronized boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * Receives a single message, assuming it's a {@link RacingEventServiceOperation}, and applies it to the
     * {@link RacingEventService} which is obtained from the service tracker passed to this replicator at construction
     * time.
     */
    @Override
    public synchronized void onMessage(Message m) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            byte[] bytesFromMessage = getBytes((BytesMessage) m);
            // Set this object's class's class loader as context for de-serialization so that all exported classes
            // of all required bundles/packages can be deserialized at least
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            ObjectInputStream ois = DomainFactory.INSTANCE.createObjectInputStreamResolvingAgainstThisFactory(
                    new ByteArrayInputStream(bytesFromMessage));
            RacingEventServiceOperation<?> operation = (RacingEventServiceOperation<?>) ois.readObject();
            applyOrQueue(operation);
        } catch (IOException | ClassNotFoundException | JMSException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
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

    private byte[] getBytes(BytesMessage m) throws JMSException {
        byte[] buf = new byte[(int) m.getBodyLength()];
        m.readBytes(buf);
        return buf;
    }

    @Override
    public String toString() {
        return "Replicator for master "+master+", queue size: "+queue.size();
    }

}
