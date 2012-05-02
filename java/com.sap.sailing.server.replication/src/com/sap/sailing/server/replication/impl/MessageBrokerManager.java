package com.sap.sailing.server.replication.impl;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

public class MessageBrokerManager {
    private final MessageBrokerConfiguration configuration;
    
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    private BrokerService broker;
    
    public MessageBrokerManager(final MessageBrokerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void startMessageBroker(boolean useJmx) throws Exception {
        broker = new BrokerService();
        broker.setBrokerName(configuration.getBrokerName());
        if (configuration.getDataStoreDirectory() != null) {
            broker.setDataDirectory(configuration.getDataStoreDirectory());
        }
        broker.setUseJmx(useJmx);
        TransportConnector transportConnector = new TransportConnector();
        transportConnector.setUri(new URI(configuration.getBrokerUrl()));
        broker.addConnector(transportConnector);
        broker.start();
    }

    public void stopMessageBroker() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    public void createAndStartConnection() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD, "failover://" + configuration.getBrokerUrl());
        connection = connectionFactory.createConnection();
        connection.start();
    }
    
    public void closeConnections() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public Session createSession(boolean transacted) throws JMSException {
        session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
        return session;
    }

    public void closeSessions() throws JMSException {
        if (session != null) {
            session.close();
        }
    }

    public Session getSession() {
        return session;
    }
}
