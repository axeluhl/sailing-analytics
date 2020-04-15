package com.sap.sse.replication.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.replication.ReplicationStatus;

public class ReplicationStatusImpl implements ReplicationStatus {
    private final boolean isReplica;
    private final boolean isReplicationStarting;
    private final boolean isSuspended;
    private final boolean isStopped;
    private final long messageQueueLength;
    private final Map<String, Boolean> isInitialLoadRunning;
    private final Map<String, Integer> operationQueueLengths;
    
    public ReplicationStatusImpl(boolean isReplica, boolean isReplicationStarting, boolean isSuspended,
            boolean isStopped, long messageQueueLength, Map<String, Boolean> isInitialLoadRunning,
            Map<String, Integer> operationQueueLengths) {
        super();
        this.isReplica = isReplica;
        this.isReplicationStarting = isReplicationStarting;
        this.isSuspended = isSuspended;
        this.isStopped = isStopped;
        this.messageQueueLength = messageQueueLength;
        this.isInitialLoadRunning = isInitialLoadRunning;
        this.operationQueueLengths = operationQueueLengths;
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
    public Integer getOperationQueueLengthsByReplicableIdAsString(String replicableIdAsString) {
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
        result.put("replicationstarting", this.isReplicationStarting());
        result.put("suspended", this.isSuspended());
        result.put("stopped", this.isStopped());
        result.put("messagequeuelength", this.getMessageQueueLength());
        final JSONArray operationQueueLengths = new JSONArray();
        result.put("operationqueuelengths", operationQueueLengths);
        for (final String replicableIdAsString : this.getReplicableIdsAsStrings()) {
            Integer queueLength = this.getOperationQueueLengthsByReplicableIdAsString(replicableIdAsString);
            if (queueLength != null) {
                final JSONObject queueLengthJSON = new JSONObject();
                queueLengthJSON.put("id", replicableIdAsString);
                queueLengthJSON.put("length", queueLength);
                operationQueueLengths.add(queueLengthJSON);
            }
        }
        result.put("totaloperationqueuelength", this.getTotalOperationQueueLength());
        for (final String replicableIdAsString : this.getReplicableIdsAsStrings()) {
            Boolean initialLoadRunning = this.isInitialLoadRunning(replicableIdAsString);
            if (initialLoadRunning != null) {
                final JSONObject replicableJSON = new JSONObject();
                replicableJSON.put("id", replicableIdAsString);
                replicableJSON.put("initialloadrunning", initialLoadRunning);
                replicablesJSON.add(replicableJSON);
            }
        }
        result.put("replicables", replicablesJSON);
        result.put("available", this.isAvailable());
        return result;
    }
}
