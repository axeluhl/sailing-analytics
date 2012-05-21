package com.sap.sailing.server.replication.impl;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

public class ReplicationMasterDescriptorImpl implements ReplicationMasterDescriptor {
    private static final String REPLICATION_SERVLET = "/replication/replication";
    private final String hostname;
    private final int servletPort;
    private final int jmsPort;
    
    public ReplicationMasterDescriptorImpl(String hostname, int servletPort, int jmsPort) {
        this.hostname = hostname;
        this.servletPort = servletPort;
        this.jmsPort = jmsPort;
    }

    @Override
    public URL getReplicationRegistrationRequestURL() throws MalformedURLException {
        return new URL("http", hostname, servletPort, REPLICATION_SERVLET + "?" + ReplicationServlet.ACTION + "="
                + ReplicationServlet.Action.REGISTER.name());
    }

    @Override
    public URL getInitialLoadURL() throws MalformedURLException {
        return new URL("http", hostname, servletPort, REPLICATION_SERVLET + "?" + ReplicationServlet.ACTION + "="
                + ReplicationServlet.Action.INITIAL_LOAD.name());
    }

    @Override
    public TopicSubscriber getTopicSubscriber(String clientID) throws JMSException, UnknownHostException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD, "tcp://" + hostname + ":" + jmsPort);
        connectionFactory.setClientID(clientID);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(ReplicationService.SAILING_SERVER_REPLICATION_TOPIC);
        return session.createDurableSubscriber(topic, InetAddress.getLocalHost().getHostAddress());
    }

    @Override
    public int getJMSPort() {
        return jmsPort;
    }

    @Override
    public int getServletPort() {
        return servletPort;
    }

    @Override
    public String getHostname() {
        return hostname;
    }
}
