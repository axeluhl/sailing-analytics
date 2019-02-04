package com.sap.sse.replication;

import org.json.simple.JSONObject;

public interface ReplicationStatus {
    boolean isReplica();
    
    boolean isReplicationStarting();
    
    boolean isSuspended();
    
    boolean isStopped();
    
    long getMessageQueueLength();
    
    Iterable<String> getReplicableIdsAsStrings();
    
    /**
     * The queue length or {@code null} if no queue exists for the replicable with ID {@link code replicableIdAsString}
     */
    Integer getOperationQueueLengthsByReplicableIdAsString(String replicableIdAsString);
    
    int getTotalOperationQueueLength();
    
    /**
     * @return {@code true} if the initial load for the replicable with ID {@link code replicableIdAsString} is
     * currently running, {@code false} if not, and {@code null} if the replicable with ID {@link code replicableIdAsString}
     * is not known.
     */
    Boolean isInitialLoadRunning(String replicableIdAsString);
    
    /**
     * An availability report based on the replication status of the overall service and all of the
     * {@link Replicable}s managed by it. This method reports {@code true} if this server is not a replica,
     * or in case it is a replica the replication is not currently starting and no initial load is currently
     * running for any replicable managed by this service.<p>
     * 
     * Note that if this server is not a replica there may still be other start-up processes going on, such as
     * the restoring of races that need to be loaded.
     */
    boolean isAvailable();

    JSONObject toJSONObject();
}
