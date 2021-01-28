package com.sap.sse.replication.impl;

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
        result.put("replica", this.isReplica());
        result.put("servername", serverName);
        result.put("replicationstarting", this.isReplicationStarting());
        result.put("suspended", this.isSuspended());
        result.put("stopped", this.isStopped());
        result.put("messagequeuelength", this.getMessageQueueLength());
        result.put("totaloperationqueuelength", this.getTotalOperationQueueLength());
        result.put("outboundmessagingname", this.outboundExchangeName);
        result.put("outboundmessagingport", this.outboundMessagingPort);
        for (final String replicableIdAsString : this.getReplicableIdsAsStrings()) {
            Boolean initialLoadRunning = this.isInitialLoadRunning(replicableIdAsString);
            if (initialLoadRunning != null) {
                final JSONObject replicableJSON = new JSONObject();
                replicableJSON.put("id", replicableIdAsString);
                replicableJSON.put("initialloadrunning", initialLoadRunning);
                final Integer operationQueueLength = this.getOperationQueueLength(replicableIdAsString);
                replicableJSON.put("operationqueuelength", operationQueueLength==null?0:operationQueueLength);
                replicableJSON.put("replicatedfrom", getReplicatedFromAsJSON(replicableIdAsString));
                replicableJSON.put("replicatedby", getReplicatedByAsJSON(replicableIdAsString));
                replicablesJSON.add(replicableJSON);
            }
        }
        result.put("replicables", replicablesJSON);
        result.put("available", this.isAvailable());
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
        result.put("exchange", masterDescriptor.getExchangeName());
        result.put("hostname", masterDescriptor.getHostname());
        result.put("port", masterDescriptor.getServletPort());
        result.put("messaginghostname", masterDescriptor.getMessagingHostname());
        result.put("messagingport", masterDescriptor.getMessagingPort());
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

    private Object serializeReplicaDescriptorAsJSON(ReplicaDescriptor replicaDescriptor) {
        final JSONObject result = new JSONObject();
        result.put("id", replicaDescriptor.getUuid().toString());
        result.put("registrationtimemillis", replicaDescriptor.getRegistrationTime().asMillis());
        result.put("address", replicaDescriptor.getIpAddress().getCanonicalHostName());
        result.put("additionalinformation", replicaDescriptor.getAdditionalInformation());
        return result;
    }
}
