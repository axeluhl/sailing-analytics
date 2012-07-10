package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class SimpleRabbitMQTest {
    private final static String QUEUE_NAME = "hello";
    
    private Map<Consumer, String> received;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    
    @Before
    public void setUp() throws IOException {
        received = new HashMap<Consumer, String>();
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
    }
    
    @After
    public void tearDown() throws IOException {
        channel.close();
        connection.close();
    }
    
    @Test
    public void testSendReceiveHelloWorld() throws IOException, InterruptedException {
        final Consumer consumer = new Consumer();
        new Thread(consumer).start();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        Thread.sleep(500);
        assertEquals(message, received.get(consumer));
    }
    
    private class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection;
                connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(QUEUE_NAME, true, consumer);
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                received.put(this, new String(delivery.getBody()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ShutdownSignalException e) {
                e.printStackTrace();
            } catch (ConsumerCancelledException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
