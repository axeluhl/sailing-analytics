package com.sap.sailing.server.replication.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationSlaveDescriptor;

public class ReplicationInstancesManager {

    /**
     * The list of descriptors of all registered slaves
     * All broadcast operations will send the messages to all registered slaves
     */
    private Map<UUID, ReplicationSlaveDescriptor> replicationSlavesDescriptors;
    
    /**
     * The descriptor of the replication master
     */
    private ReplicationMasterDescriptor replicationMasterDescriptor;

    public ReplicationInstancesManager() {
        replicationSlavesDescriptors = new HashMap<UUID, ReplicationSlaveDescriptor>();
    }
    
    public Iterator<ReplicationSlaveDescriptor> getSlavesDescriptors() {
        return replicationSlavesDescriptors.values().iterator();
    }

    public ReplicationMasterDescriptor getReplicationMasterDescriptor() {
        return replicationMasterDescriptor;
    }
    
    public ReplicationSlaveDescriptor findSlaveDescriptor(UUID slaveUUID) {
        return replicationSlavesDescriptors.get(slaveUUID);
    }
    
    public ReplicationSlaveDescriptor addSlave(UUID slaveUUID) {
        ReplicationSlaveDescriptor replicationSlaveDescriptor = replicationSlavesDescriptors.get(slaveUUID);
        if(replicationSlaveDescriptor == null) {
            replicationSlaveDescriptor = new ReplicationSlaveDescriptor(slaveUUID); 
        }
        return replicationSlaveDescriptor;
    }

    public ReplicationSlaveDescriptor removeSlave(UUID slaveUUID) {
        return replicationSlavesDescriptors.remove(slaveUUID);
    }
}
