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
    private final int messagingPort;
    
    /**
     * @param messagingPort 0 means use default port
     */
    public ReplicationMasterDescriptorImpl(String hostname, String exchangeName, int servletPort, int messagingPort) {
        this.hostname = hostname;
        this.servletPort = servletPort;
        this.messagingPort = messagingPort;
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
        int port = getMessagingPort();
        if (port != 0) {
            connectionFactory.setPort(port);
        }
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "fanout");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "");
        channel.basicConsume(queueName, consumer);
        return consumer;
    }

    /**
     * @return 0 means use default port
     */
    @Override
    public int getMessagingPort() {
        return messagingPort;
    }

    @Override
    public int getServletPort() {
        return servletPort;
    }

    @Override
    public String getHostname() {
        return hostname;
    }
    
    @Override
    public String getExchangeName() {
        return exchangeName;
    }
}
