package com.sap.sse.gwt.client.replication;

import com.sap.sse.gwt.shared.replication.ReplicationStateDTO;

public interface RemoteReplicationService {
    ReplicationStateDTO getReplicaInfo();
    
    /**
     * @param usernameOrNull
     *            may be null or empty if no authentication is required to replicate from the remote server
     * @param passwordOrNull
     *            may be null or empty if no authentication is required to replicate from the remote server
     */
    void startReplicatingFromMaster(String messagingHost, String masterHostName, String exchangeName, int servletPort,
            int messagingPort, String usernameOrNull, String passwordOrNull) throws Exception;

    void stopAllReplicas();

    void stopReplicatingFromMaster();

    void stopSingleReplicaInstance(String identifier);

    String[] getReplicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica();
}
