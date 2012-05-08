package com.sap.sailing.server.replication.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class ReplicationInstancesManager {

    /**
     * The set of descriptors of all registered slaves. All broadcast operations will send the messages to all
     * registered slaves, assuming the slaves will have subscribed for the replication topic.
     */
    private Set<ReplicaDescriptor> replicaDescriptors;
    
    /**
     * The descriptor of the replication master
     */
    private ReplicationMasterDescriptor replicationMasterDescriptor;

    public ReplicationInstancesManager() {
        replicaDescriptors = new HashSet<ReplicaDescriptor>();
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
    
    public Iterator<ReplicaDescriptor> getReplicaDescriptors() {
        return replicaDescriptors.iterator();
    }

    public ReplicationMasterDescriptor getReplicationMasterDescriptor() {
        return replicationMasterDescriptor;
    }
    
    public void registerReplica(ReplicaDescriptor replica) {
        replicaDescriptors.add(replica);
    }

    public void unregisterReplica(ReplicaDescriptor replica) {
        replicaDescriptors.remove(replica);
    }
}
