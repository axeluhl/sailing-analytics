package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sap.sailing.server.replication.impl.AMPQInputStream;
import com.sap.sailing.server.replication.impl.AMPQOutputStream;

/**
 * A number of tests for the {@link OutputStream} / {@link InputStream} implementation using an underlying RabbitMQ message queue.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RemoteMessageStreamTest {
    private Channel channel;
    private AMPQOutputStream outputStream;
    
    @Before
    public void setUp() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
    }
    
    @Test
    public void testBasicConnectivityInOnePacket() throws IOException {
        outputStream = new AMPQOutputStream(/* messageSizeInBytes */ 8192, channel, /* syncAfterTimeout */ false);
        AMPQInputStream inputStream = new AMPQInputStream(channel, outputStream.getQueueName());
        final String message = "Hello World!";
        outputStream.write(message.getBytes());
        outputStream.close();
        byte[] buf = new byte[100];
        int numberOfReadBytes = inputStream.getInputStream().read(buf);
        assertEquals(message.getBytes().length, numberOfReadBytes);
        byte[] trimmedMessage = new byte[numberOfReadBytes];
        System.arraycopy(buf, 0, trimmedMessage, 0, numberOfReadBytes);
        String messageAsString = new String(trimmedMessage);
        assertEquals(message, messageAsString);
    }
}
