package com.sap.sailing.server.replication;

import com.sap.sailing.server.replication.impl.ReplicationFactoryImpl;

public interface ReplicationFactory {
    static ReplicationFactory INSTANCE = new ReplicationFactoryImpl();
    
    ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname, String exchangeName, int servletPort, int jmsPort, String jmsQueueName);
}
