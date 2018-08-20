package com.sap.sse.replication;

import com.sap.sse.replication.impl.ReplicationFactoryImpl;

public interface ReplicationFactory {
    static ReplicationFactory INSTANCE = new ReplicationFactoryImpl();
    
    ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname, String exchangeName, int servletPort, int jmsPort, String jmsQueueName, Iterable<Replicable<?, ?>> replicables);
}
