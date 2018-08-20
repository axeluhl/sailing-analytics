package com.sap.sse.replication.impl;

import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationFactory;
import com.sap.sse.replication.ReplicationMasterDescriptor;

public class ReplicationFactoryImpl implements ReplicationFactory {
    @Override
    public ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname,
            String exchangeName, int servletPort, int messagingPort, String queueName,
            Iterable<Replicable<?, ?>> replicables) {
        return new ReplicationMasterDescriptorImpl(messagingHostname, exchangeName, messagingPort, queueName, hostname, servletPort, replicables);
    }

}
