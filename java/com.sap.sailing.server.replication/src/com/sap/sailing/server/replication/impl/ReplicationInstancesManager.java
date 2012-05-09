package com.sap.sailing.server.replication.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class ReplicationInstancesManager {

    /**
     * The set of descriptors of all registered slaves. All broadcast operations will send the messages to all
     * registered slaves, assuming the slaves will have subscribed for the replication topic.
     */
    private Set<ReplicaDescriptor> replicaDescriptors;
    
    private Map<ReplicaDescriptor, Map<Class<? extends RacingEventServiceOperation<?>>, Integer>> replicationCounts;
    
    /**
     * The descriptor of the replication master
     */
    private ReplicationMasterDescriptor replicationMasterDescriptor;

    public ReplicationInstancesManager() {
        replicaDescriptors = new HashSet<ReplicaDescriptor>();
        replicationCounts = new HashMap<ReplicaDescriptor, Map<Class<? extends RacingEventServiceOperation<?>>,Integer>>();
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
        return Collections.unmodifiableCollection(replicaDescriptors);
    }

    public ReplicationMasterDescriptor getReplicationMasterDescriptor() {
        return replicationMasterDescriptor;
    }
    
    public void registerReplica(ReplicaDescriptor replica) {
        replicaDescriptors.add(replica);
        replicationCounts.put(replica, new HashMap<Class<? extends RacingEventServiceOperation<?>>, Integer>());
    }

    public void unregisterReplica(ReplicaDescriptor replica) {
        replicaDescriptors.remove(replica);
        replicationCounts.remove(replica);
    }
    
    /**
     * For {@link #replicaDescriptors each replica currently registered}, increases the replication count for the
     * type of <code>replicatedOperation</code> by one.
     * 
     * @see #getStatistics
     */
    public <T> void log(RacingEventServiceOperation<T> replicatedOperation) {
        for (ReplicaDescriptor replica : getReplicaDescriptors()) {
            Map<Class<? extends RacingEventServiceOperation<?>>, Integer> counts = replicationCounts.get(replica);
            if (counts == null) {
                counts = new HashMap<Class<? extends RacingEventServiceOperation<?>>, Integer>();
                replicationCounts.put(replica, counts);
            }
            @SuppressWarnings("unchecked") // safe because replicatedOperation is declared of type RacingEventserviceOperation<T>
            Class<? extends RacingEventServiceOperation<T>> operationClass = (Class<? extends RacingEventServiceOperation<T>>) replicatedOperation
                    .getClass();
            Integer count = counts.get(operationClass);
            if (count == null) {
                count = 0;
            }
            count++;
            counts.put(operationClass, count);
        }
    }
    
    public Map<Class<? extends RacingEventServiceOperation<?>>, Integer> getStatistics(ReplicaDescriptor replica) {
        return replicationCounts.get(replica);
    }
}
