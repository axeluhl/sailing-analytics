package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * Input stream that reads messages from AMP queue and puts them into a byte stream.
 * 
 * @author Simon Marcel Pamies
 */
public class AMPQInputStream extends InputStream {

    private static final byte TERMINATION_COMMAND[] = new byte[] { 2, 6, 0, 4, 1, 9, 8, 2, 0, 1, 4, 2 };

    private final QueueingConsumer messageConsumer;
    private final ArrayList<Byte> streamBuffer;
    private final Runnable reader;

    /**
     * Fair lock for making sure that reading from message stream does not interfere with read operation.
     */
    private final NamedReentrantReadWriteLock readWriteLock;

    public AMPQInputStream(QueueingConsumer consumer) {
        super();
        this.messageConsumer = consumer;
        this.streamBuffer = new ArrayList<Byte>();
        this.readWriteLock = new NamedReentrantReadWriteLock("Lock for message reader on " + consumer.getConsumerTag(), /* fair */
                true);
        this.reader = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    LockUtil.lockForWrite(readWriteLock);
                    try {
                        Delivery delivery = messageConsumer.nextDelivery();
                        byte[] bytesFromMessage = delivery.getBody();
                        if (bytesFromMessage.length != TERMINATION_COMMAND.length
                                && Arrays.equals(bytesFromMessage, TERMINATION_COMMAND)) {
                            for (int i = 0; i < bytesFromMessage.length; i++) {
                                streamBuffer.add(bytesFromMessage[i]);
                            }
                        } else {
                            // termination sequence received - stop receiving messages
                            break;
                        }
                    } catch (ShutdownSignalException | ConsumerCancelledException e) {
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        LockUtil.unlockAfterWrite(readWriteLock);
                    }
                }
            }
        };
        new Thread(reader).run(); // start receiving messages right away
    }

    @Override
    public int read() throws IOException {
        int result = 0;
        LockUtil.lockForWrite(readWriteLock); // need write lock because we're removing items
        try {
            if (!streamBuffer.isEmpty()) {
                result = streamBuffer.remove(0).intValue();
            }
        } finally {
            LockUtil.unlockAfterWrite(readWriteLock);
        }
        return result; // can be 0 when the stream buffer is empty
    }

}