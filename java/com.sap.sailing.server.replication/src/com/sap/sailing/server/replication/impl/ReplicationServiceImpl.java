package com.sap.sailing.server.replication.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.ReplicationSlaveDescriptor;

public class ReplicationServiceImpl implements ReplicationService {
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final MessageBrokerManager messageBrokerManager;
    
    private Topic replicationTopic;
    
    private final boolean isMaster;
    
    public ReplicationServiceImpl(final ReplicationInstancesManager replicationInstancesManager,
            final MessageBrokerManager messageBrokerManager, boolean isMaster) {
        this.replicationInstancesManager = replicationInstancesManager;
        this.messageBrokerManager = messageBrokerManager;
        this.isMaster = isMaster;
    }
    
    public ReplicationSlaveDescriptor registerSlave(UUID slaveId) {
        return replicationInstancesManager.addSlave(slaveId);

    }
        
    public ReplicationSlaveDescriptor unregisterSlave(UUID slaveId) {
        return replicationInstancesManager.removeSlave(slaveId);
    }

    private Topic getReplicationTopic() throws JMSException{
        if (replicationTopic== null) {
            Session session = messageBrokerManager.getSession();
            if(session == null) {
                session = messageBrokerManager.createSession(true);
            }
            replicationTopic = session.createTopic("SailingServerReplicationTopic");
        }
        return replicationTopic;
    }
    
    public void broadcastOperation(RacingEventServiceOperation<?> operation) throws Exception {
        Topic topic = getReplicationTopic();
        Session session = messageBrokerManager.getSession();
        MessageProducer producer = messageBrokerManager.getSession().createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = session.createTextMessage("Hello World!");
        System.out.println("Sending message: " + message.getText());
        producer.send(message);
    }

    @Override
    public List<String> getHostnamesOfReplica() {
        List<String> result = new ArrayList<String>();
        for (Iterator<ReplicationSlaveDescriptor> i=replicationInstancesManager.getSlavesDescriptors(); i.hasNext(); ) {
            ReplicationSlaveDescriptor d = i.next();
            result.add(d.getIpAddress());
        }
        return result;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }
    
}
