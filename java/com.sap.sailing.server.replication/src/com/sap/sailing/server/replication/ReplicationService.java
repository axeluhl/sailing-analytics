package com.sap.sailing.server.replication;

import java.util.List;

import com.sap.sailing.server.RacingEventServiceOperation;

public interface ReplicationService {
    void broadcastOperation(RacingEventServiceOperation<?> operation) throws Exception;
    
    List<String> getHostnamesOfReplica();

    boolean isMaster();
}
