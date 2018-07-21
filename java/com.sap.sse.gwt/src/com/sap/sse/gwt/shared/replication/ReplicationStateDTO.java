package com.sap.sse.gwt.shared.replication;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds information about a replica.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ReplicationStateDTO implements IsSerializable {
    private Map<String, ReplicaDTO> replicaInfoByHostname;
    private ReplicationMasterDTO replicatingFromMaster;
    private String serverIdentifier;
    
    ReplicationStateDTO() { } // for de-serialization
    
    public ReplicationStateDTO(ReplicationMasterDTO replicatingFromMaster, Iterable<ReplicaDTO> replicas, String serverIdentifier) {
        this.replicatingFromMaster = replicatingFromMaster;
        this.serverIdentifier = serverIdentifier;
        this.replicaInfoByHostname = new HashMap<String, ReplicaDTO>();
        for (ReplicaDTO replica : replicas) {
            replicaInfoByHostname.put(replica.getIdentifier(), replica);
        }
    }

    public ReplicationMasterDTO getReplicatingFromMaster() {
        return replicatingFromMaster;
    }

    public Iterable<ReplicaDTO> getReplicas() {
        return replicaInfoByHostname.values();
    }
    
    public String getServerIdentifier() {
        return serverIdentifier;
    }
}