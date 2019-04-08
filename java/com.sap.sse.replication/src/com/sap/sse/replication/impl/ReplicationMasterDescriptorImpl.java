package com.sap.sse.replication.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sse.ServerInfo;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationMasterDescriptor;

/**
 * Equality is defined by the master's hostname / port, the messaging hostname / port and the
 * messaging exchange name, defining the sequence of messages received. The individual message
 * consumer and queue name, however, are not part of the equality definition. Therefore, two
 * descriptors may differ in their technical connection to the master but would still be
 * considered equal as long as they will receive the same set of messages from the same
 * master.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ReplicationMasterDescriptorImpl implements ReplicationMasterDescriptor {
    private static final Logger logger = Logger.getLogger(ReplicationMasterDescriptorImpl.class.getName());
    
    private static final String REPLICATION_SERVLET = "/replication/replication";
    private final String masterServletHostname;
    private final String exchangeName;
    private final int servletPort;
    private final String messagingHostname;
    private final int messagingPort;
    private final String queueName;
    private final Iterable<Replicable<?, ?>> replicables;

    private QueueingConsumer consumer;

    /**
     * @param messagingHostname
     *            name of the host on which the exchange is hosted to which this replica connects with a queue whose
     *            name is provided in the <code>queueName</code> parameter
     * @param exchangeName
     *            the name of the fan-out exchange used by the remote master to write its replication operations to
     * @param messagingPort
     *            0 means use default port
     * @param queueName
     *            the name for the queue that this replication client uses to connect to the remote exchange; it is
     *            helpful if this queue name uniquely identifies the replica because the queue name will appear in tools
     *            for monitoring the messaging infrastructure
     * @param masterServletHostname
     *            host to connect to for HTTP requests to the master that trigger the replication and register the
     *            replica with the master
     * @param servletPort
     *            port for HTTP requests to the master
     * @param replicables
     *            the {@link Replicable} objects to replicate from the master described by this object; the master will
     *            send operations only for those replicables that at least one replica is registered for; this, however,
     *            may mean that the master also sends operations for replicables that a particular replica hasn't
     *            registered for. Replicas shall silently drop operations for such replicables that they haven't
     *            requested replication for.
     */
    public ReplicationMasterDescriptorImpl(String messagingHostname, String exchangeName, int messagingPort,
            String queueName, String masterServletHostname, int servletPort, Iterable<Replicable<?, ?>> replicables) {
        this.masterServletHostname = masterServletHostname;
        this.messagingHostname = messagingHostname;
        this.servletPort = servletPort;
        this.messagingPort = messagingPort;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.consumer = null;
        this.replicables = replicables;
    }

    @Override
    public URL getReplicationRegistrationRequestURL(UUID uuid, String additional) throws MalformedURLException,
            UnsupportedEncodingException {
        final String[] replicableIdsAsString = StreamSupport.stream(replicables.spliterator(), /* parallel */ false).map(r->r.getId()).toArray(i->new String[i]);
        return new URL("http", getHostname(), servletPort, REPLICATION_SERVLET + "?" + ReplicationServlet.ACTION + "="
                + ReplicationServlet.Action.REGISTER.name() + "&" + ReplicationServlet.SERVER_UUID + "="
                + java.net.URLEncoder.encode(uuid.toString(), "UTF-8") + "&"
                + ReplicationServlet.ADDITIONAL_INFORMATION + "="
                + java.net.URLEncoder.encode(ServerInfo.getBuildVersion(), "UTF-8") + "&"
                + ReplicationServlet.REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED + "="
                + java.net.URLEncoder.encode(String.join(",", replicableIdsAsString), "UTF-8"));
    }

    @Override
    public URL getReplicationDeRegistrationRequestURL(UUID uuid) throws MalformedURLException {
        return new URL("http", getHostname(), servletPort, REPLICATION_SERVLET + "?" + ReplicationServlet.ACTION + "="
                + ReplicationServlet.Action.DEREGISTER.name() + "&" + ReplicationServlet.SERVER_UUID + "="
                + uuid.toString());
    }

    @Override
    public Iterable<Replicable<?, ?>> getReplicables() {
        return replicables;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exchangeName == null) ? 0 : exchangeName.hashCode());
        result = prime * result + ((masterServletHostname == null) ? 0 : masterServletHostname.hashCode());
        result = prime * result + ((messagingHostname == null) ? 0 : messagingHostname.hashCode());
        result = prime * result + messagingPort;
        result = prime * result + servletPort;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReplicationMasterDescriptorImpl other = (ReplicationMasterDescriptorImpl) obj;
        if (exchangeName == null) {
            if (other.exchangeName != null)
                return false;
        } else if (!exchangeName.equals(other.exchangeName))
            return false;
        if (masterServletHostname == null) {
            if (other.masterServletHostname != null)
                return false;
        } else if (!masterServletHostname.equals(other.masterServletHostname))
            return false;
        if (messagingHostname == null) {
            if (other.messagingHostname != null)
                return false;
        } else if (!messagingHostname.equals(other.messagingHostname))
            return false;
        if (messagingPort != other.messagingPort)
            return false;
        if (servletPort != other.servletPort)
            return false;
        return true;
    }

    @Override
    public URL getInitialLoadURL(Iterable<Replicable<?, ?>> replicables) throws MalformedURLException {
        final String replicablesIdsAsStringSeparatedByCommas = StreamSupport.stream(replicables.spliterator(), /* parallel */ false).
            map(r->r.getId().toString()).collect(Collectors.joining(","));
        return new URL("http", getHostname(), servletPort, REPLICATION_SERVLET + "?" + ReplicationServlet.ACTION + "="
                + ReplicationServlet.Action.INITIAL_LOAD.name() +
                "&"+ReplicationServlet.REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED+"="+replicablesIdsAsStringSeparatedByCommas);
    }
    
    @Override
    public URL getSendReplicaInitiatedOperationToMasterURL(String replicableIdAsString) throws MalformedURLException {
        return new URL("http", getHostname(), servletPort, REPLICATION_SERVLET);
    }

    @Override
    public synchronized QueueingConsumer getConsumer() throws IOException {
        Channel channel = createChannel();
        /*
         * Connect a queue to the given exchange that has already been created by the master server.
         */
        channel.exchangeDeclare(exchangeName, "fanout");
        QueueingConsumer consumer = new QueueingConsumer(channel);

        /*
         * The x-message-ttl argument to queue.declare controls for how long a message published to a queue can live
         * before it is discarded. A message that has been in the queue for longer than the configured TTL is said to be
         * dead. Note that a message routed to multiple queues can die at different times, or not at all, in each queue
         * in which it resides. The death of a message in one queue has no impact on the life of the same message in
         * other queues.
         */
        final Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl", (60 * 30) * 1000); // messages will live half an hour in queue before being deleted

        /*
         * The x-expires argument to queue.declare controls for how long a queue can be unused before it is
         * automatically deleted. Unused means the queue has no consumers, the queue has not been redeclared, and
         * basic.get has not been invoked for a duration of at least the expiration period.
         */
        args.put("x-expires", (60 * 60) * 1000); // queue will live one hour before being deleted

        /*
         * The maximum length of a queue can be limited to a set number of messages by supplying the x-max-length queue
         * declaration argument with a non-negative integer value. Queue length is a measure that takes into account
         * ready messages, ignoring unacknowledged messages and message size. Messages will be dropped or dead-lettered
         * from the front of the queue to make room for new messages once the limit is reached.
         */
        args.put("x-max-length", 3000000);

        // a server-named non-exclusive, non-durable queue
        // this queue will survive a connection drop (autodelete=false) and
        // will also support being reconnected (exclusive=false). it will
        // not survive a rabbitmq server restart (durable=false).
        String queueName = channel.queueDeclare(this.queueName,
        /* durable */false, /* exclusive */false, /* auto-delete */false, args).getQueue();

        // from now on we get all new messages that the exchange is getting from producer
        channel.queueBind(queueName, exchangeName, "");
        channel.basicConsume(queueName, /* auto-ack */true, consumer);
        this.consumer = consumer;
        return consumer;
    }

    @Override
    public Channel createChannel() throws IOException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(getMessagingHostname());
        int port = getMessagingPort();
        if (port != 0) {
            connectionFactory.setPort(port);
        }
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

    @Override
    public synchronized void stopConnection(boolean deleteExchange) {
        try {
            if (consumer != null) {
                // make sure to remove queue in order to avoid any exchanges filling it with messages
                consumer.getChannel().queueUnbind(queueName, exchangeName, "");
                consumer.getChannel().queueDelete(queueName);
                if (deleteExchange) {
                    consumer.getChannel().exchangeDelete(exchangeName);
                }
                consumer.getChannel().getConnection().close(/* timeout in millis */ 1000);
            }
        } catch (Exception ex) {
            // ignore any exception during abort. close can yield a broad
            // number of exceptions that we don't want to know or to log.
            logger.log(Level.SEVERE, "Exception while closing replication channel consumer", ex);
        }
    }
    
    /**
     * @return 0 means use default port
     */
    @Override
    public int getMessagingPort() {
        return messagingPort;
    }

    @Override
    public String getMessagingHostname() {
        return messagingHostname;
    }

    @Override
    public int getServletPort() {
        return servletPort;
    }

    @Override
    public String getHostname() {
        return masterServletHostname;
    }

    @Override
    public String getExchangeName() {
        return exchangeName;
    }

    public String toString() {
        return getHostname() + ":" + getServletPort() + " / " + getMessagingHostname() + ":" + getMessagingPort()+":"+getExchangeName();
    }

}
