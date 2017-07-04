package com.sap.sse.gwt.client.replication;

import com.sap.sse.gwt.shared.replication.ReplicationStateDTO;

public interface RemoteReplicationService {
    ReplicationStateDTO getReplicaInfo();
    
    void startReplicatingFromMaster(String messagingHost, String masterName, String exchangeName, int servletPort, int messagingPort) throws Exception;

    void stopAllReplicas();

    void stopReplicatingFromMaster();

    void stopSingleReplicaInstance(String identifier);

    String[] getReplicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica();
}
