package com.sap.sailing.server.replication;

import java.io.Serializable;

import com.sap.sailing.server.RacingEventServiceOperation;

public interface ServerReplicationMasterService {
    void broadcastOperation(RacingEventServiceOperation<?> operation) throws Exception;
    
    void broadcastInitialServerState(Serializable state) throws Exception;
}
