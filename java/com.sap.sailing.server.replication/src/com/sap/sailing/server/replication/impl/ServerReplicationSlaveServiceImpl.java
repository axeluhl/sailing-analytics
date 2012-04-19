package com.sap.sailing.server.replication.impl;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.sap.sailing.server.replication.ReplicationSlaveDescriptor;
import com.sap.sailing.server.replication.ServerReplicationSlaveService;

public class ServerReplicationSlaveServiceImpl implements ServerReplicationSlaveService {
    private final ReplicationInstancesManager replicationInstancesManager;

    public ServerReplicationSlaveServiceImpl(ReplicationInstancesManager replicationInstancesManager) {
        this.replicationInstancesManager = replicationInstancesManager;
    }
    
    public ReplicationSlaveDescriptor registerSlave(UUID slaveId) {
        return replicationInstancesManager.addSlave(slaveId);

    }
        
    public ReplicationSlaveDescriptor unregisterSlave(UUID slaveId) {
        return replicationInstancesManager.removeSlave(slaveId);
    }
    
//    private void createMessageConsumer() throws JMSException, InterruptedException {
//        MessageConsumer consumer = session.createConsumer(destination);
//        SampleMessageConsumer messageConsumer = new SampleMessageConsumer();
//        connection.setExceptionListener(messageConsumer);
//        consumer.setMessageListener(messageConsumer);
//    }
}
