package com.sap.sailing.server.replication.impl;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.sap.sailing.server.operationaltransformation.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ServerReplicationMasterService;

public class ServerReplicationMasterServiceImpl implements ServerReplicationMasterService {
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final MessageBrokerManager messageBrokerManager;
    
    private Topic replicationTopic;
    
    public ServerReplicationMasterServiceImpl(final ReplicationInstancesManager replicationInstancesManager, final MessageBrokerManager messageBrokerManager) {
        this.replicationInstancesManager = replicationInstancesManager;
        this.messageBrokerManager = messageBrokerManager;
    }
    
    private Topic getReplicationTopic() throws JMSException{
        if(replicationTopic== null) {
            Session session = messageBrokerManager.getSession();
            if(session == null) {
                session = messageBrokerManager.createSession(true);
            }
            replicationTopic = session.createTopic("SailingServerReplicationTopic");
        }
        return replicationTopic;
    }
    
    public void broadcastOperation(RacingEventServiceOperation operation) throws Exception {
        Topic topic = getReplicationTopic();
        Session session = messageBrokerManager.getSession();
        
        MessageProducer producer = messageBrokerManager.getSession().createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = session.createTextMessage("Hello World!");
        System.out.println("Sending message: " + message.getText());
        producer.send(message);
    }
    
    public void broadcastInitialServerState(Serializable state) throws Exception {
        return;
    }
}
