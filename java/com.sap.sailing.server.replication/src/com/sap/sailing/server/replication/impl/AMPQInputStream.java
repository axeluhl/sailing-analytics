package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.sap.sailing.domain.common.impl.NamedImpl;

/**
 * Input stream that reads messages from AMP queue and puts them into a byte stream.
 * 
 * @author Simon Marcel Pamies
 * @author Axel Uhl
 */
public class AMPQInputStream extends NamedImpl {
    private static final Logger logger = Logger.getLogger(AMPQInputStream.class.getName());
    private static final long serialVersionUID = 1342935135386887494L;

    private final PipedInputStream clientReadsFromThis;
    private final PipedOutputStream messagesAreWrittenToThis;
    
    public AMPQInputStream(Channel channel, String queueName) throws IOException {
        this(channel, queueName, /* name */ UUID.randomUUID().toString());
    }
    
    public AMPQInputStream(Channel channel, String queueName, String name) throws IOException {
        super(name);
        assert name != null;
        messagesAreWrittenToThis = new PipedOutputStream();
        clientReadsFromThis = new PipedInputStream(messagesAreWrittenToThis);
        final QueueingConsumer messageConsumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, /* auto-ack */ true, messageConsumer);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Delivery delivery = messageConsumer.nextDelivery();
                        byte[] bytesFromMessage = delivery.getBody();
                        if (bytesFromMessage.length != AMPQOutputStream.TERMINATION_COMMAND.length
                                || !Arrays.equals(bytesFromMessage, AMPQOutputStream.TERMINATION_COMMAND)) {
                            messagesAreWrittenToThis.write(bytesFromMessage);
                        } else {
                            // termination sequence received - stop receiving messages
                            messagesAreWrittenToThis.close();
                            break;
                        }
                    } catch (ShutdownSignalException | ConsumerCancelledException e) {
                        logger.log(Level.INFO, "Problem with message queue "+getName(), e);
                        break;
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Reading of next message in stream "+getName()+" Interrupted; continuing", e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public InputStream getInputStream() {
        return clientReadsFromThis;
    }
}