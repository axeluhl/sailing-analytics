package com.sap.sailing.server.replication.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

/**
 * Receives {@link RacingEventServiceOperation}s through JMS and
 * {@link RacingEventService#apply(RacingEventServiceOperation) applies} them to the {@link RacingEventService} passed
 * to this replicator at construction.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Replicator implements MessageListener {
    private final ReplicationMasterDescriptor master;
    private final HasRacingEventService racingEventServiceTracker;
    
    public Replicator(ReplicationMasterDescriptor master, HasRacingEventService racingEventServiceTracker) {
        this.master = master;
        this.racingEventServiceTracker = racingEventServiceTracker;
    }

    /**
     * Receives a single message, assuming it's a {@link RacingEventServiceOperation}, and applies it to the
     * {@link RacingEventService} which is obtained from the service tracker passed to this replicator at construction
     * time.
     */
    @Override
    public void onMessage(Message m) {
        try {
            RacingEventServiceOperation<?> operation = (RacingEventServiceOperation<?>) ((ObjectMessage) m).getObject();
            racingEventServiceTracker.getRacingEventService().apply(operation);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return "Replicator for master "+master;
    }

}
