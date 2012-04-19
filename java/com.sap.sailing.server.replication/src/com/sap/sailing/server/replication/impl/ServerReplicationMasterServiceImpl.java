package com.sap.sailing.server.replication.impl;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.sap.sailing.server.operationaltransformation.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ServerReplicationMasterService;

public class ServerReplicationMasterServiceImpl implements ServerReplicationMasterService {
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final MessageBrokerManager messageBrokerManager;
    
    private Queue replicationQueue;
    
    public ServerReplicationMasterServiceImpl(final ReplicationInstancesManager replicationInstancesManager, final MessageBrokerManager messageBrokerManager) {
        this.replicationInstancesManager = replicationInstancesManager;
        this.messageBrokerManager = messageBrokerManager;
    }
    
    private Queue getReplicationQueue() throws JMSException{
        if(replicationQueue == null) {
            Session session = messageBrokerManager.getSession();
            if(session == null) {
                session = messageBrokerManager.createSession(true);
            }
            replicationQueue = session.createQueue("SailingServerManyPlayersQueue");
        }
        return replicationQueue;
    }
    
    public void broadcastOperation(RacingEventServiceOperation operation) throws Exception {
        Queue queue = getReplicationQueue();
        Session session = messageBrokerManager.getSession();
        
        MessageProducer producer = messageBrokerManager.getSession().createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = session.createTextMessage("Hello World!");
        System.out.println("Sending message: " + message.getText());
        producer.send(message);
    }
    
    public void broadcastInitialServerState(Serializable state) throws Exception {
        return;
    }
}
