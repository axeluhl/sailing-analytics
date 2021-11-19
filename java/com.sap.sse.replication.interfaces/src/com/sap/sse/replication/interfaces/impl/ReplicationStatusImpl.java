package com.sap.sse.replication.interfaces.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Util;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationStatus;

public class ReplicationStatusImpl implements ReplicationStatus {
    private final boolean isReplica;
    private final String serverName;
    private final boolean isReplicationStarting;
    private final boolean isSuspended;
    private final boolean isStopped;
    private final long messageQueueLength;
    private final Map<String, Boolean> isInitialLoadRunning;
    private final Map<String, Integer> operationQueueLengths;
    private final ReplicationMasterDescriptor masterDescriptor;
    private final Iterable<ReplicaDescriptor> replicaDescriptors;
    private final String outboundExchangeName;
    private final int outboundMessagingPort;
    
    public ReplicationStatusImpl(boolean isReplica, String serverName, boolean isReplicationStarting,
            boolean isSuspended, boolean isStopped, long messageQueueLength, Map<String, Boolean> isInitialLoadRunning,
            Map<String, Integer> operationQueueLengths, ReplicationMasterDescriptor masterDescriptor,
            Iterable<ReplicaDescriptor> replicaDescriptors, String outboundExchangeName, int outboundMessagingPort) {
        super();
        this.isReplica = isReplica;
        this.serverName = serverName;
        this.isReplicationStarting = isReplicationStarting;
        this.isSuspended = isSuspended;
        this.isStopped = isStopped;
        this.messageQueueLength = messageQueueLength;
        this.isInitialLoadRunning = isInitialLoadRunning;
        this.operationQueueLengths = operationQueueLengths;
        this.masterDescriptor = masterDescriptor;
        this.replicaDescriptors = replicaDescriptors;
        this.outboundExchangeName = outboundExchangeName;
        this.outboundMessagingPort = outboundMessagingPort;
    }
    
    @Override
    public boolean isReplica() {
        return isReplica;
    }

    @Override
    public boolean isReplicationStarting() {
        return isReplicationStarting;
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public long getMessageQueueLength() {
        return messageQueueLength;
    }

    @Override
    public Iterable<String> getReplicableIdsAsStrings() {
        return Collections.unmodifiableMap(isInitialLoadRunning).keySet();
    }

    @Override
    public Integer getOperationQueueLength(String replicableIdAsString) {
        return operationQueueLengths.get(replicableIdAsString);
    }

    @Override
    public int getTotalOperationQueueLength() {
        int result = 0;
        for (Entry<String, Integer> e : operationQueueLengths.entrySet()) {
            result += e.getValue();
        }
        return result;
    }

    @Override
    public Boolean isInitialLoadRunning(String replicableIdAsString) {
        return isInitialLoadRunning.get(replicableIdAsString);
    }

    @Override
    public boolean isAvailable() {
        return !isReplica() || (!isReplicationStarting() && !isInitialLoadRunning());
    }

    /**
     * Tells whether the initial load is running for any of the replicables known
     */
    private boolean isInitialLoadRunning() {
        boolean result = false;
        for (boolean initialLoadRunning : isInitialLoadRunning.values()) {
            result |= initialLoadRunning;
        }
        return result;
    }

    @Override
    public JSONObject toJSONObject() {
        final JSONObject result = new JSONObject();
        final JSONArray replicablesJSON = new JSONArray();
        result.put(JSON_FIELD_NAME_REPLICA, this.isReplica());
        result.put(JSON_FIELD_NAME_SERVERNAME, serverName);
        result.put(JSON_FIELD_NAME_REPLICATIONSTARTING, this.isReplicationStarting());
        result.put(JSON_FIELD_NAME_SUSPENDED, this.isSuspended());
        result.put(JSON_FIELD_NAME_STOPPED, this.isStopped());
        result.put(JSON_FIELD_NAME_MESSAGEQUEUELENGTH, this.getMessageQueueLength());
        result.put(JSON_FIELD_NAME_TOTALOPERATIONQUEUELENGTH, this.getTotalOperationQueueLength());
        result.put(JSON_FIELD_NAME_OUTBOUNDMESSAGINGNAME, this.outboundExchangeName);
        result.put(JSON_FIELD_NAME_OUTBOUNDMESSAGINGPORT, this.outboundMessagingPort);
        for (final String replicableIdAsString : this.getReplicableIdsAsStrings()) {
            Boolean initialLoadRunning = this.isInitialLoadRunning(replicableIdAsString);
            if (initialLoadRunning != null) {
                final JSONObject replicableJSON = new JSONObject();
                replicableJSON.put(JSON_FIELD_NAME_REPLICABLE_ID, replicableIdAsString);
                replicableJSON.put(JSON_FIELD_NAME_REPLICABLE_INITIALLOADRUNNING, initialLoadRunning);
                final Integer operationQueueLength = this.getOperationQueueLength(replicableIdAsString);
                replicableJSON.put(JSON_FIELD_NAME_REPLICABLE_OPERATIONQUEUELENGTH, operationQueueLength==null?0:operationQueueLength);
                replicableJSON.put(JSON_FIELD_NAME_REPLICABLE_REPLICATEDFROM, getReplicatedFromAsJSON(replicableIdAsString));
                replicableJSON.put(JSON_FIELD_NAME_REPLICABLE_REPLICATEDBY, getReplicatedByAsJSON(replicableIdAsString));
                replicablesJSON.add(replicableJSON);
            }
        }
        result.put(JSON_FIELD_NAME_REPLICABLES, replicablesJSON);
        result.put(JSON_FIELD_NAME_AVAILABLE, this.isAvailable());
        return result;
    }

    private JSONObject getReplicatedFromAsJSON(String replicableIdAsString) {
        final JSONObject result;
        if (masterDescriptor != null && Util.contains(Util.map(masterDescriptor.getReplicables(), r->r.getId().toString()), replicableIdAsString)) {
            result = serializeMasterDescriptorAsJSON();
        } else {
            result = null;
        }
        return result;
    }
    
    private JSONObject serializeMasterDescriptorAsJSON() {
        final JSONObject result = new JSONObject();
        result.put(JSON_FIELD_NAME_EXCHANGE, masterDescriptor.getExchangeName());
        result.put(JSON_FIELD_NAME_HOSTNAME, masterDescriptor.getHostname());
        result.put(JSON_FIELD_NAME_PORT, masterDescriptor.getServletPort());
        result.put(JSON_FIELD_NAME_MESSAGINGHOSTNAME, masterDescriptor.getMessagingHostname());
        result.put(JSON_FIELD_NAME_MESSAGINGPORT, masterDescriptor.getMessagingPort());
        return result;
    }

    private JSONArray getReplicatedByAsJSON(String replicableIdAsString) {
        final JSONArray result = new JSONArray();
        for (final ReplicaDescriptor replicaDescriptor : replicaDescriptors) {
            if (Arrays.asList(replicaDescriptor.getReplicableIdsAsStrings()).contains(replicableIdAsString)) {
                result.add(serializeReplicaDescriptorAsJSON(replicaDescriptor));
            }
        }
        return result;
    }

    private JSONObject serializeReplicaDescriptorAsJSON(ReplicaDescriptor replicaDescriptor) {
        final JSONObject result = new JSONObject();
        result.put(JSON_FIELD_NAME_REPLICABLE_ID, replicaDescriptor.getUuid().toString());
        result.put(JSON_FIELD_NAME_REGISTRATIONTIMEMILLIS, replicaDescriptor.getRegistrationTime().asMillis());
        result.put(JSON_FIELD_NAME_PORT, replicaDescriptor.getPort());
        result.put(JSON_FIELD_NAME_ADDRESS, replicaDescriptor.getIpAddress().getHostAddress());
        result.put(JSON_FIELD_NAME_ADDITIONALINFORMATION, replicaDescriptor.getAdditionalInformation());
        return result;
    }
}
