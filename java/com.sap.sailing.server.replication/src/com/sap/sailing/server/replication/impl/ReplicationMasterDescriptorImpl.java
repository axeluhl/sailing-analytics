package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class ReplicationMasterDescriptorImpl implements ReplicationMasterDescriptor {
    private static final String REPLICATION_SERVLET = "/replication/replication";
    private final String hostname;
    private final String exchangeName;
    private final int servletPort;
    private final int jmsPort;
    
    public ReplicationMasterDescriptorImpl(String hostname, String exchangeName, int servletPort, int jmsPort) {
        this.hostname = hostname;
        this.servletPort = servletPort;
        this.jmsPort = jmsPort;
        this.exchangeName = exchangeName;
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
    public QueueingConsumer getConsumer() throws IOException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(getHostname());
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "fanout");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "");
        channel.basicConsume(queueName, consumer);
        return consumer;
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
