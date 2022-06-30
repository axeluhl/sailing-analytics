package com.sap.sse.replication;

import java.util.Map;

public interface ReplicationReceiver {
    /**
     * A string that is sent for new replication protocol versions instead of the stringified {@link Replicable} ID.
     * That replicable ID is usually a fully-qualified class name. It must not be equal to this version indicator string
     * which has the value {@code "***"}.
     */
    static String VERSION_INDICATOR = "***";

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
