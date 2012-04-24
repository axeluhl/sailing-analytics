package com.sap.sailing.server.replication.impl;

import com.sap.sailing.server.replication.ReplicationFactory;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class ReplicationFactoryImpl implements ReplicationFactory {

    @Override
    public ReplicationMasterDescriptor createReplicationMasterDescriptor(String hostname, int servletPort, int jmsPort) {
        return new ReplicationMasterDescriptorImpl(hostname, servletPort, jmsPort);
    }

}
