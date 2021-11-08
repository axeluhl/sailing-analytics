package com.sap.sse.replication;

import java.util.Map;

public interface ReplicationReceiver {

    /**
     * If the replicator is suspended, messages received are queued.
     */
    boolean isSuspended();

    boolean isBeingStopped();

    /**
     * @return the number of unprocessed messages in the inbound message queue
     */
    int getMessageQueueSize() throws IllegalAccessException;

    /**
     * @return the number of queued {@link OperationWithResult operations}, keyed by the stringified IDs of the
     *         replicables to which the queue belongs
     */
    Map<String, Integer> getOperationQueueSizes();
    
    /**
     * @return <code>true</code> if all queues for all replicables are empty
     */
    boolean isQueueEmptyOrStopped() throws IllegalAccessException;

}
