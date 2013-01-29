package com.sap.sailing.gwt.ui.shared;

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
    
    ReplicationStateDTO() { } // for de-serialization
    
    public ReplicationStateDTO(ReplicationMasterDTO replicatingFromMaster, Iterable<ReplicaDTO> replicas) {
        this.replicatingFromMaster = replicatingFromMaster;
        this.replicaInfoByHostname = new HashMap<String, ReplicaDTO>();
        for (ReplicaDTO replica : replicas) {
            replicaInfoByHostname.put(replica.getHostname(), replica);
        }
    }

    public ReplicationMasterDTO getReplicatingFromMaster() {
        return replicatingFromMaster;
    }

    public Iterable<ReplicaDTO> getReplicas() {
        return replicaInfoByHostname.values();
    }
    
    public ReplicaDTO getReplicaByHostname(String hostname) {
        return replicaInfoByHostname.get(hostname);
    }
}