package com.sap.sailing.server.replication.impl;

import com.sap.sailing.server.replication.ReplicationFactory;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class ReplicationFactoryImpl implements ReplicationFactory {
    @Override
    public ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname, String exchangeName, int servletPort, int messagingPort, String queueName) {
        return new ReplicationMasterDescriptorImpl(messagingHostname, exchangeName, messagingPort, queueName, hostname, servletPort);
    }

}
