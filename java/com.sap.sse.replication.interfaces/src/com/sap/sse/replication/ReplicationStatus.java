package com.sap.sse.replication;

import org.json.simple.JSONObject;

public interface ReplicationStatus {
    static final String JSON_FIELD_NAME_ADDITIONALINFORMATION = "additionalinformation";
    static final String JSON_FIELD_NAME_ADDRESS = "address";
    static final String JSON_FIELD_NAME_REGISTRATIONTIMEMILLIS = "registrationtimemillis";
    static final String JSON_FIELD_NAME_MESSAGINGPORT = "messagingport";
    static final String JSON_FIELD_NAME_MESSAGINGHOSTNAME = "messaginghostname";
    static final String JSON_FIELD_NAME_PORT = "port";
    static final String JSON_FIELD_NAME_HOSTNAME = "hostname";
    static final String JSON_FIELD_NAME_EXCHANGE = "exchange";
    static final String JSON_FIELD_NAME_AVAILABLE = "available";
    static final String JSON_FIELD_NAME_REPLICABLES = "replicables";
    static final String JSON_FIELD_NAME_REPLICABLE_REPLICATEDBY = "replicatedby";
    static final String JSON_FIELD_NAME_REPLICABLE_REPLICATEDFROM = "replicatedfrom";
    static final String JSON_FIELD_NAME_REPLICABLE_OPERATIONQUEUELENGTH = "operationqueuelength";
    static final String JSON_FIELD_NAME_REPLICABLE_INITIALLOADRUNNING = "initialloadrunning";
    static final String JSON_FIELD_NAME_REPLICABLE_ID = "id";
    static final String JSON_FIELD_NAME_OUTBOUNDMESSAGINGPORT = "outboundmessagingport";
    static final String JSON_FIELD_NAME_OUTBOUNDMESSAGINGNAME = "outboundmessagingname";
    static final String JSON_FIELD_NAME_TOTALOPERATIONQUEUELENGTH = "totaloperationqueuelength";
    static final String JSON_FIELD_NAME_MESSAGEQUEUELENGTH = "messagequeuelength";
    static final String JSON_FIELD_NAME_STOPPED = "stopped";
    static final String JSON_FIELD_NAME_SUSPENDED = "suspended";
    static final String JSON_FIELD_NAME_REPLICATIONSTARTING = "replicationstarting";
    static final String JSON_FIELD_NAME_SERVERNAME = "servername";
    static final String JSON_FIELD_NAME_REPLICA = "replica";

    boolean isReplica();
    
    boolean isReplicationStarting();
    
    boolean isSuspended();
    
    boolean isStopped();
    
    long getMessageQueueLength();
    
    Iterable<String> getReplicableIdsAsStrings();
    
    /**
     * The queue length or {@code null} if no queue exists for the replicable with ID {@link code replicableIdAsString}
     */
    Integer getOperationQueueLength(String replicableIdAsString);
    
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

    /**
     * A JSON representation of this object.<p>
     */
    JSONObject toJSONObject();
}
