package com.sap.sse.gwt.client.replication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.shared.replication.ReplicationStateDTO;

public interface RemoteReplicationServiceAsync {
    void getReplicaInfo(AsyncCallback<ReplicationStateDTO> callback);
    
    void startReplicatingFromMaster(String messagingHost, String masterName, String exchangeName, int servletPort,
            int messagingPort, String usernameOrNull, String passwordOrNull, AsyncCallback<Void> callback);

    void stopAllReplicas(AsyncCallback<Void> asyncCallback);

    void stopReplicatingFromMaster(AsyncCallback<Void> asyncCallback);

    void stopSingleReplicaInstance(String identifier, AsyncCallback<Void> asyncCallback);

    void getReplicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica(AsyncCallback<String[]> markedAsyncCallback);

}
