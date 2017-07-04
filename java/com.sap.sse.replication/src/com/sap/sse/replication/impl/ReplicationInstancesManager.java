package com.sap.sse.replication.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicationMasterDescriptor;

/**
 * Manages the replicas and keeps statistics.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the state to which replication operations are applied
 */
public class ReplicationInstancesManager {

    /**
     * The set of descriptors of all registered slaves. All broadcast operations will send the messages to all
     * registered slaves, assuming the slaves will have subscribed for the replication topic. Keys are the
     * {@link ReplicaDescriptor#getUuid() IDs of the corresponding values}.
     */
    private Map<UUID, ReplicaDescriptor> replicaDescriptors;
    
    private Map<ReplicaDescriptor, Map<Class<? extends OperationWithResult<?, ?>>, Integer>> replicationCounts;
    
    /**
     * Used to calculate the average number of operations per message sent/received for each replica.
     * 
     * @see #totalMessageCount
     */
    private final Map<ReplicaDescriptor, Long> totalNumberOfOperations;

    /**
     * Used to calculate the average number of operations per message sent/received for each replica.
     * 
     * @see #totalNumberOfOperations
     */
    private final Map<ReplicaDescriptor, Long> totalMessageCount;
    
    /**
     * Contains the size of all messages sent over
     */
    private final Map<ReplicaDescriptor, Long> totalQueueMessagesRawSizeInBytes;
    
    /**
     * The descriptor of the replication master
     */
    private ReplicationMasterDescriptor replicationMasterDescriptor;

    public ReplicationInstancesManager() {
        replicaDescriptors = new HashMap<>();
        replicationCounts = new HashMap<ReplicaDescriptor, Map<Class<? extends OperationWithResult<?, ?>>,Integer>>();
        totalMessageCount = new HashMap<>();
        totalNumberOfOperations = new HashMap<>();
        totalQueueMessagesRawSizeInBytes = new HashMap<ReplicaDescriptor, Long>();
    }
    
    /**
     * Tells if at least one replica is currently registered.
     * 
     * @see #registerReplica(ReplicaDescriptor)
     * @see #unregisterReplica(ReplicaDescriptor)
     */
    public boolean hasReplicas() {
        return !replicaDescriptors.isEmpty();
    }
    
    public Iterable<ReplicaDescriptor> getReplicaDescriptors() {
        return Collections.unmodifiableCollection(replicaDescriptors.values());
    }

    public ReplicationMasterDescriptor getReplicationMasterDescriptor() {
        return replicationMasterDescriptor;
    }
    
    public Iterable<String> getAllReplicableIdsAtLeastOneReplicaIsReplicating() {
        return replicaDescriptors.values().stream().map(replicableDescriptor->Arrays.asList(replicableDescriptor.getReplicableIdsAsStrings())).reduce(new HashSet<String>(),
                (result, ids)->{result.addAll(ids); return result;}, (r1, r2)->{r1.addAll(r2); return r1;});
    }
    
    public void registerReplica(ReplicaDescriptor replica) {
        replicaDescriptors.put(replica.getUuid(), replica);
        replicationCounts.put(replica, new HashMap<>());
    }

    public void unregisterReplica(ReplicaDescriptor replica) {
        unregisterReplica(replica.getUuid());
    }
    
    public ReplicaDescriptor unregisterReplica(UUID replicaUuid) {
        final ReplicaDescriptor replica = replicaDescriptors.remove(replicaUuid);
        replicationCounts.remove(replica);
        return replica;
    }
    
    /**
     * For {@link #replicaDescriptors each replica currently registered}, increases the replication count for the
     * type of <code>replicatedOperation</code> by one.
     * 
     * @see #getStatistics
     */
    public void log(List<Class<?>> classes, long sizeOfQueueMessageInBytes) {
        for (ReplicaDescriptor replica : getReplicaDescriptors()) {
            Map<Class<? extends OperationWithResult<?, ?>>, Integer> counts = replicationCounts.get(replica);
            if (counts == null) {
                counts = new HashMap<Class<? extends OperationWithResult<?, ?>>, Integer>();
                replicationCounts.put(replica, counts);
            }
            for (Class<?> replicatedOperationClass : classes) {
                // safe because replicatedOperation is declared of type RacingEventserviceOperation<T>
                @SuppressWarnings("unchecked")
                Class<? extends OperationWithResult<?, ?>> operationClass = (Class<? extends OperationWithResult<?, ?>>) replicatedOperationClass;
                Integer count = counts.get(operationClass);
                if (count == null) {
                    count = 0;
                }
                counts.put(operationClass, ++count);
            }
            Long totalOps = totalNumberOfOperations.get(replica);
            if (totalOps == null) {
                totalOps = 0l;
            }
            totalOps += Util.size(classes);
            totalNumberOfOperations.put(replica, totalOps);
            Long totalMessages = totalMessageCount.get(replica);
            if (totalMessages == null) {
                totalMessages = 0l;
            }
            totalMessageCount.put(replica, ++totalMessages);
            Long totalQueueMessagesSizeInBytes = totalQueueMessagesRawSizeInBytes.get(replica);
            if (totalQueueMessagesSizeInBytes == null) {
                totalQueueMessagesSizeInBytes = 0l;
            }
            totalQueueMessagesRawSizeInBytes.put(replica, totalQueueMessagesSizeInBytes+sizeOfQueueMessageInBytes);
        }
    }
    
    public Map<Class<? extends OperationWithResult<?, ?>>, Integer> getStatistics(ReplicaDescriptor replica) {
        return replicationCounts.get(replica);
    }
    
    public long getNumberOfBytesSent(ReplicaDescriptor replica) {
        final long result;
        Long totalQueueMessageSize = totalQueueMessagesRawSizeInBytes.get(replica);
        if (totalQueueMessageSize == null) {
            result = 0l;
        } else {
            result = totalQueueMessageSize.longValue();
        }
        return result;
    }
    
    public double getAverageNumberOfOperationsPerMessage(ReplicaDescriptor replica) {
        final double result;
        Long messageCount = totalMessageCount.get(replica);
        if (messageCount != null) {
            Long operationsCount = totalNumberOfOperations.get(replica);
            if (operationsCount == null) {
                result = 0;
            } else {
                result = ((double) operationsCount)/(double) messageCount;
            }
        } else {
            result = 0;
        }
        return result;
    }
    
    public double getAverageNumberOfBytesPerMessage(ReplicaDescriptor replica) {
        final double result;
        Long messageCount = totalMessageCount.get(replica);
        if (messageCount != null) {
            Long messageSizeInTotal = totalQueueMessagesRawSizeInBytes.get(replica);
            if (messageSizeInTotal == null) {
                result = 0;
            } else {
                result = ((double) messageSizeInTotal)/(double) messageCount;
            }
        } else {
            result = 0;
        }
        return result;
    }
    
    public long getNumberOfMessagesSent(ReplicaDescriptor replica) {
        final long result;
        Long messageCount = totalMessageCount.get(replica);
        if (messageCount != null) {
            result = messageCount.longValue();
        } else {
            result = 0l;
        }
        return result;
    }

    public void removeAll() {
        replicationCounts.clear();
        replicaDescriptors.clear();
    }
}
