package com.sap.sailing.server.replication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.rabbitmq.client.QueueingConsumer;

/**
 * Identifies a master server instance from which a replica can obtain an initial load and continuous updates.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public interface ReplicationMasterDescriptor {

    URL getReplicationRegistrationRequestURL(UUID uuid, String additionalInformation) throws MalformedURLException, UnsupportedEncodingException;
    
    URL getReplicationDeRegistrationRequestURL(UUID uuid) throws MalformedURLException;

    URL getInitialLoadURL() throws MalformedURLException;

    int getMessagingPort();
    
    int getServletPort();

    String getHostname();

    /**
     * Creates a queue, declares the master's fanout exchange on the calling client and binds the queue to the exchange.
     * Then, adds a consumer to the queue just created and starts consuming. The caller may keep calling
     * {@link QueueingConsumer#nextDelivery()} on the consumer returned in order to obtain the next message.
     */
    QueueingConsumer getConsumer() throws IOException;

    String getExchangeName();

    void stopConnection();

    String getMessagingHostname();
}
