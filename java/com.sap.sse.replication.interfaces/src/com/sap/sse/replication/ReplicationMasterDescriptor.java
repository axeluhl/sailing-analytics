package com.sap.sse.replication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Identifies a master server instance from which a replica can obtain an initial load and continuous updates.
 * 
 * TODO bug 2465: add the set of {@link Replicable}s that are replicated from the master represented by this descriptor,
 * considering that this may be a subset only of the replicables running on this instance or the master server. Example:
 * replicating only the SecurityService from some other server but being a master regarding all other Replicables.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public interface ReplicationMasterDescriptor {

    URL getReplicationRegistrationRequestURL(UUID uuid, String additionalInformation) throws MalformedURLException, UnsupportedEncodingException;
    
    URL getReplicationDeRegistrationRequestURL(UUID uuid) throws MalformedURLException;

    /**
     * The content produced by the URL returned is the name of the queue from which the client can read the initial load
     * using a {@link RabbitInputStreamProvider}. Handled by the {@link ReplicationServlet}.
     * 
     * @param replicables
     *            the replicables for which to request the initial load
     */
    URL getInitialLoadURL(Iterable<Replicable<?, ?>> replicables) throws MalformedURLException;

    /**
     * The {@link ReplicationServlet} handles sending an operation for execution from a replica that initiated it to
     * a master where processing and replication along the complete replica tree shall start. This method returns the
     * {@link URL} that must be used in POST mode to send the serialized operation to the servlet where it will be
     * de-serialized and applied to the {@link Replicable} identified by the <code>replicableIdAsString</code>.
     */
    URL getSendReplicaInitiatedOperationToMasterURL(String replicableIdAsString) throws MalformedURLException;

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

    /**
     * @param deleteExchange
     *            Only to be used by the master itself when no longer delivering messages to the exchange, or to tear
     *            down after a test
     */
    void stopConnection(boolean deleteExchange);

    String getMessagingHostname();

    /**
     * @return a RabbitMQ channel created with the replication connectivity parameters defined by this descriptor
     */
    Channel createChannel() throws IOException;

    Iterable<Replicable<?, ?>> getReplicables();

}
