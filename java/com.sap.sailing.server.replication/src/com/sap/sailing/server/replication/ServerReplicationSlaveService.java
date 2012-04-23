package com.sap.sailing.server.replication;

import java.util.UUID;

public interface ServerReplicationSlaveService {
    ReplicationSlaveDescriptor registerSlave(UUID slaveId);

    ReplicationSlaveDescriptor unregisterSlave(UUID slaveId);
}
