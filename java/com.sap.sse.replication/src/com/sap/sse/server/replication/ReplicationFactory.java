package com.sap.sse.server.replication;

import com.sap.sse.server.replication.impl.ReplicationFactoryImpl;

public interface ReplicationFactory {
    static ReplicationFactory INSTANCE = new ReplicationFactoryImpl();
    
    ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname, String exchangeName, int servletPort, int jmsPort, String jmsQueueName);
}
