package com.sap.sse.replication.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.osgi.util.tracker.ServiceTracker;

import com.rabbitmq.client.AMQP.Exchange;
import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sse.BuildVersion;
import com.sap.sse.common.Util;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicablesProvider;
import com.sap.sse.replication.ReplicablesProvider.ReplicableLifeCycleListener;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;

/**
 * Manages a set of observers of {@link Replicable}, receiving notifications for the operations they perform that
 * require replication. This service provides the connectivity and central management including central keeping of
 * replication statistics for this server instance. Triggering the broadcast of an operation notified by a
 * {@link Replicable} is in the responsibility of each individual observer.
 * <p>
 * 
 * The observers are registered only when there are replicas registered. If the last replica is de-registered, the
 * service stops observing the {@link Replicable}. Operations received that require replication are sent to the
 * {@link Exchange} to which replica queues can bind, using a {@link Replicator}. By prefixing each message with the
 * {@link Object#toString()} representation of the {@link Replicable}'s {@link Replicable#getId() ID} the receiver can
 * determine to which {@link Replicable} to forward the operation. As such, this service multiplexes the replication
 * channels for potentially many {@link Replicable}s living in this server instance.
 * <p>
 * 
 * The exchange name and connectivity information for the message queuing system are provided to this service during
 * construction.
 * <p>
 * 
 * This service object {@link Replicable#addOperationExecutionListener(OperationExecutionListener) registers} individual
 * listeners at the {@link Replicable}s so it {@link #executed(OperationWithResult) receives} notifications about
 * operations executed by the {@link Replicable} that require replication if and only if there is at least one replica
 * registered.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class ReplicationServiceImpl implements ReplicationService {
    private static final Logger logger = Logger.getLogger(ReplicationServiceImpl.class.getName());
    
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final ReplicablesProvider replicablesProvider;
    
    /**
     * <code>null</code>, if this instance is not currently replicating from some master; the master's descriptor otherwise
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    
    /**
     * The UUIDs with which this replica is registered by the master identified by the corresponding key
     */
    private final Map<ReplicationMasterDescriptor, String> replicaUUIDs;
    
    /**
     * Channel used by a master server to publish replication operations; <code>null</code> in servers that don't have replicas registered
     */
    private Channel masterChannel;
    
    /**
     * The name of the RabbitMQ exchange to which this replication service sends its replication operations in
     * serialized form. Clients need to know this name to be able to bind their queues to the exchange.
     */
    private final String exchangeName;
    
    /**
     * The host on which the RabbitMQ server runs
     */
    private final String exchangeHost;
    
    /**
     * The port on which to reach the RabbitMQ server, or 0 for the RabbitMQ default port
     */
    private final int exchangePort;
    
    /**
     * UUID that identifies this server
     */
    private final UUID serverUUID;
    
    /**
     * For this instance running as a replica, the replicator receives messages from the master's queue and applies them
     * to the local replica.
     */
    private Replicator replicator;
    
    private final Set<ReplicationServiceExecutionListener<?>> executionListeners;
    
    private Thread replicatorThread;
    
    /**
     * Used to synchronize write access and replacements of {@link #outboundBuffer}, {@link #outboundObjectBuffer} and
     * {@link #outboundBufferClasses} when the timer scoops up the messages to send.
     */
    private final Object outboundBufferMonitor = "";
    
    /**
     * Sending operations as serialized Java objects using binary RabbitMQ messages comes at an overhead. To reduce the overhead,
     * several operations can be serialized into a single message. The actual serialization of the buffer happens after a short duration
     * has passed since the last sending, managed by a {@link Timer}. Writers need to synchronize on {@link #outboundBufferMonitor}
     * which protects all of {@link #outboundBuffer}, {@link #outboundObjectBuffer} and {@link #outboundBufferClasses} which
     * are replaced or cleared when the timer scoops up the currently buffered operations to send them out.
     */
    private ByteArrayOutputStream outboundBuffer;
    
    /**
     * The {@link #outboundBuffer} contains a message with serialized operations originating from a single
     * {@link Replicable} whose {@link Replicable#getId() ID} is written as its string value to the beginning
     * of the stream using {@link DataOutputStream#writeUTF(String)}. When an operation of a {@link Replicable}
     * with a different ID is to be {@link #broadcastOperation(OperationWithResult, Replicable) broadcast}, the
     * existing {@link #outboundBuffer} needs to be transmitted and a new one is started for the {@link Replicable}
     * now wanting to replicate an operation. Access is synchronized, as for {@link #outboundBuffer} using the
     * {@link #outboundBufferMonitor}.
     */
    private String outboundBufferReplicableIdAsString;

    /**
     * An object output stream that writes to {@link #outboundBuffer}. Operations are serialized into this stream until the timer
     * acquires the {@link #outboundBufferMonitor}, closes the stream and transmits the contents of {@link #outboundBuffer} as a
     * RabbitMQ message. While still holding the monitor, the timer task creates a new {@link #outboundBuffer} and a new
     * {@link #outboundObjectBuffer} wrapping the {@link #outboundBuffer}.
     */
    private ObjectOutputStream outboundObjectBuffer;
    
    /**
     * Remembers the classes of the operations serialized into {@link #outboundObjectBuffer}. The list of classes in this list
     * matches with the sequence of objects written to {@link #outboundObjectBuffer} as long as the {@link #outboundBufferMonitor}
     * is being held.
     */
    private List<Class<?>> outboundBufferClasses;
    
    /**
     * Used to schedule the sending of all operations in {@link #outboundBuffer} using the {@link #sendingTask}.
     */
    private final Timer timer;
    
    /**
     * Sends all operations in {@link #outboundBuffer}. When holding the monitor of {@link #outboundBuffer},
     * the following rules hold:
     * 
     * <ul>
     *   <li>if <code>null</code>, adding an operation to {@link #outboundBuffer} needs to create and assign a new timer that
     *       schedules a sending task.
     *   </li>
     *   <li>if not <code>null</code>, an operation added to {@link #outboundBuffer} is guaranteed to be sent by the timer
     *   </li>
     * </ul>
     * 
     */
    private TimerTask sendingTask;
    
    /**
     * Defines for how many milliseconds the {@link #timer} will wait since the first operation has been added to an empty
     * {@link #outboundBuffer} until it carries out the actual transmission task. The longer this duration, the more operations
     * are likely to be sent per message transmitted, reducing overhead but correspondingly increasing latency.
     */
    private final long TRANSMISSION_DELAY_MILLIS = 100;
    
    /**
     * Defines at which message size in bytes the message will be sent regardless the {@link #TRANSMISSION_DELAY_MILLIS}.
     */
    private final int TRIGGER_MESSAGE_SIZE_IN_BYTES = 1024*1024;

    /**
     * Counts the messages sent out by this replicator
     */
    private long messageCount;
    
    private class LifeCycleListener implements ReplicableLifeCycleListener {
        @Override
        public void replicableAdded(Replicable<?, ?> replicable) {
            addNewOperationExecutionListener(replicable);
        }

        @Override
        public void replicableRemoved(Replicable<?, ?> replicable) {
            // TODO
        }
    }
    
    /**
     * @param exchangeName
     *            name of the fan-out exchange to which replication operations will be sent
     * @param exchangeHost
     *            name of the host under which the RabbitMQ server can be reached
     * @param exchangePort
     *            port of the RabbitMQ server, or 0 for default port
     * @param replicablesProvider
     *            lets this service request the currently known {@link Replicable} objects to be observed for
     *            replication; it also offers this service the possibility to register for life cycle events of those
     *            {@link Replicable} objects so that this service can stop observing them for operations to be
     *            replicated when they have been removed, or start observing new {@link Recpliable} objects that were
     *            added.
     */
    public ReplicationServiceImpl(String exchangeName, String exchangeHost,
            int exchangePort, final ReplicationInstancesManager replicationInstancesManager,
            ReplicablesProvider replicablesProvider) throws IOException {
        timer = new Timer("ReplicationServiceImpl timer for delayed task sending");
        executionListeners = new HashSet<>();
        this.replicationInstancesManager = replicationInstancesManager;
        replicaUUIDs = new HashMap<ReplicationMasterDescriptor, String>();
        this.replicablesProvider = replicablesProvider;
        this.exchangeName = exchangeName;
        this.exchangeHost = exchangeHost;
        this.exchangePort = exchangePort;
        replicablesProvider.addReplicableLifeCycleListener(new LifeCycleListener());
        replicator = null;
        serverUUID = UUID.randomUUID();
        logger.info("Setting " + serverUUID.toString() + " as unique replication identifier.");
        
    }

    protected ServiceTracker<Replicable<?, ?>, Replicable<?, ?>> getReplicableTracker() {
        return new ServiceTracker<Replicable<?, ?>, Replicable<?, ?>>(
                Activator.getDefaultContext(), Replicable.class.getName(), null);
    }
    
    private Channel createMasterChannelAndDeclareFanoutExchange() throws IOException {
        Channel result = createMasterChannel();
        result.exchangeDeclare(exchangeName, "fanout");
        logger.info("Created fanout exchange "+exchangeName+" successfully.");
        return result;
    }

    @Override
    public Channel createMasterChannel() throws IOException, ConnectException {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(exchangeHost);
        if (exchangePort != 0) {
            connectionFactory.setPort(exchangePort);
        }
        Channel result = null;
        try {
            result = connectionFactory.newConnection().createChannel();
        } catch (ConnectException ex) {
            // make sure to log something meaningful
            logger.severe("Could not connect to messaging queue on " + connectionFactory.getHost() + ":" + connectionFactory.getPort() + "/" + exchangeName);
            throw ex;
        }
        logger.info("Connected to " + connectionFactory.getHost() + ":" + connectionFactory.getPort());
        return result;
    }

    private Iterable<Replicable<?, ?>> getReplicables() {
        return replicablesProvider.getReplicables();
    }

    @Override
    public void registerReplica(ReplicaDescriptor replica) throws IOException {
        if (!replicationInstancesManager.hasReplicas()) {
            addAsListenerToReplicables();
            synchronized (this) {
                if (masterChannel == null) {
                    masterChannel = createMasterChannelAndDeclareFanoutExchange();
                }
            }
        }
        replicationInstancesManager.registerReplica(replica);
        logger.info("Registered replica " + replica);
    }
    
    private void addAsListenerToReplicables() {
        for (Replicable<?, ?> replicable : getReplicables()) {
            addNewOperationExecutionListener(replicable);
        }
    }

    private <S> void addNewOperationExecutionListener(Replicable<S, ?> replicable) {
        final ReplicationServiceExecutionListener<S> listener = new ReplicationServiceExecutionListener<S>(this, replicable);
        executionListeners.add(listener);
    }

    @Override
    public void unregisterReplica(ReplicaDescriptor replica) throws IOException {
        logger.info("Unregistering replica " + replica);
        replicationInstancesManager.unregisterReplica(replica);
        if (!replicationInstancesManager.hasReplicas()) {
            removeAsListenerFromReplicables();
            synchronized (this) {
                if (masterChannel != null) {
                    masterChannel.close();
                    masterChannel = null;
                }
            }
        }
    }

    private void removeAsListenerFromReplicables() {
        for (ReplicationServiceExecutionListener<?> listener : executionListeners) {
            listener.unsubscribe();
        }
    }

    /**
     * Schedules a single operation for broadcast. The operation is added to {@link #outboundBuffer}, and if not already
     * scheduled, a {@link #timer} is created and scheduled to send in {@link #TRANSMISSION_DELAY_MILLIS} milliseconds.
     * 
     * @param replicable
     *            the replicable by which the operation was executed that now will be broadcast to all replicas; the {@link Replicable#getId() ID}
     *            of this replica in its string form will be used to prefix the operation
     */
    <S, O extends OperationWithResult<S, ?>> void broadcastOperation(OperationWithResult<?, ?> operation, Replicable<S, O> replicable) throws IOException {
        // need to write the operations one by one, making sure the ObjectOutputStream always writes
        // identical objects again if required because they may have changed state in between
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(operation);
        oos.close();
        final byte[] serializedOperation = bos.toByteArray();
        synchronized (outboundBufferMonitor) {
            final String replicaIdAsString = replicable.getId().toString();
            if (outboundBuffer != null && !Util.equalsWithNull(outboundBufferReplicableIdAsString, replicaIdAsString)) {
                flushBufferToRabbitMQ(); // operation from a replicable different from that for which operations are buffered so far --> flush
            } // still holding the monitor, so no other broadcast request from a different replicable can step in between
            if (outboundBuffer == null) {
                outboundBuffer = new ByteArrayOutputStream();
                outboundBufferReplicableIdAsString = replicaIdAsString;
                GZIPOutputStream zipper = new GZIPOutputStream(outboundBuffer);
                new DataOutputStream(zipper).writeUTF(replicaIdAsString);
                outboundObjectBuffer = new ObjectOutputStream(zipper);
                outboundBufferClasses = new ArrayList<>();
            }
            outboundObjectBuffer.writeObject(serializedOperation);
            outboundBufferClasses.add(operation.getClass());
            if (outboundBuffer.size() > TRIGGER_MESSAGE_SIZE_IN_BYTES) {
                logger.info("Triggering replication because buffer holds " + outboundBuffer.size()
                        + " bytes which exceeds trigger size " + TRIGGER_MESSAGE_SIZE_IN_BYTES+" bytes");
                flushBufferToRabbitMQ();
            } else {
                if (sendingTask == null) {
                    sendingTask = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                sendingTask = null;
                                logger.fine("Running timer task, flushing buffer");
                                flushBufferToRabbitMQ();
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Exception while trying to replicate operations", e);
                            }
                        }
                    };
                    timer.schedule(sendingTask, TRANSMISSION_DELAY_MILLIS);
                }
            }
            if (++messageCount % 10000l == 0) {
                logger.info("Handled "+messageCount+" messages for replication. Current outbound replication queue size: "+outboundBufferClasses.size());
            }
        }
    }
    
    /**
     * Obtains the monitor on {@link #outboundBufferMonitor}, copies the references to the buffers, nulls out
     * the buffers, then releases the monitor and broadcasts the buffer.
     */
    private void flushBufferToRabbitMQ() {
        logger.fine("Trying to acquire monitor");
        final byte[] bytesToSend;
        final List<Class<?>> classesOfOperationsToSend;
        final boolean doSend;
        synchronized (outboundBufferMonitor) {
            if (outboundBuffer != null) {
                logger.fine("Preparing " + outboundBufferClasses.size() + " operations for sending to RabbitMQ exchange");
                try {
                    outboundObjectBuffer.close();
                    logger.fine("Sucessfully closed ObjectOutputStream");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error trying to replicate " + outboundBufferClasses.size()
                            + " operations", e);
                }
                bytesToSend = outboundBuffer.toByteArray();
                logger.fine("Successfully produced bytesToSend array of length " + bytesToSend.length);
                classesOfOperationsToSend = outboundBufferClasses;
                doSend = true;
                outboundBuffer = null;
                outboundBufferReplicableIdAsString = null;
                outboundObjectBuffer = null;
                outboundBufferClasses = null;
            } else {
                logger.fine("No buffer set; probably two timer tasks were scheduled concurrently. No problem, just not sending this time around.");
                doSend = false;
                bytesToSend = null;
                classesOfOperationsToSend = null;
            }
        }
        if (doSend) {
            try {
                broadcastOperations(bytesToSend, classesOfOperationsToSend);
                logger.fine("Successfully handed " + classesOfOperationsToSend.size() + " operations to broadcaster");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to replicate " + classesOfOperationsToSend.size() + " operations", e);
            }
        }
    }

    private void broadcastOperations(byte[] bytesToSend, List<Class<?>> classesOfOperationsToSend) throws IOException {
        logger.fine("broadcasting "+classesOfOperationsToSend.size()+" operations as "+bytesToSend.length+" bytes");
        if (masterChannel != null) {
            logger.fine("buffer to broadcast has "+bytesToSend.length+" bytes ("+(bytesToSend.length/1024/1024)+"MB)");
            long startTime = System.currentTimeMillis();
            masterChannel.basicPublish(exchangeName, /* routingKey */"", /* properties */null, bytesToSend);
            logger.fine("successfully published "+bytesToSend.length+" bytes, taking "+(System.currentTimeMillis()-startTime)+"ms");
            replicationInstancesManager.log(classesOfOperationsToSend, bytesToSend.length);
        }
    }

    @Override
    public Iterable<ReplicaDescriptor> getReplicaInfo() {
        return replicationInstancesManager.getReplicaDescriptors();
    }

    @Override
    public ReplicationMasterDescriptor getReplicatingFromMaster() {
        return replicatingFromMaster;
    }

    /**
     * The peer for this method is
     * {@link ReplicationServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * which implements the initial load sending process.
     */
    @Override
    public void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, InterruptedException {
        logger.info("Starting to replicate from "+master);
        try {
            registerReplicaWithMaster(master);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "ERROR", ex);
            throw ex;
        }
        replicatingFromMaster = master;
        logger.info("Registered replica with master.");
        QueueingConsumer consumer = null;
        // logging exception here because it will not propagate
        // thru the client with all details
        try {
            logger.info("Connecting to message queue " + master);
            consumer = master.getConsumer();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "ERROR", ex);
            replicatingFromMaster = null;
            throw ex;
        }
        logger.info("Connection to exchange successful.");
        final Iterable<Replicable<?, ?>> replicables = replicablesProvider.getReplicables();
        URL initialLoadURL = master.getInitialLoadURL(replicables);
        logger.info("Initial load URL is "+initialLoadURL);
        // start receiving messages already now, but start in suspended mode
        replicator = new Replicator(master, replicablesProvider, /* startSuspended */true, consumer);
        // clear Replicable state here, before starting to receive and de-serialized operations which builds up
        // new state, e.g., in competitor store
        for (Replicable<?, ?> r : getReplicables()) {
            r.clearReplicaState();
        }
        replicatorThread = new Thread(replicator, "Replicator receiving from " + master.getMessagingHostname() + "/"
                + master.getExchangeName());
        replicatorThread.start();
        logger.info("Started replicator thread");
        InputStream is = initialLoadURL.openStream();
        final String queueName = new BufferedReader(new InputStreamReader(is)).readLine();
        RabbitInputStreamProvider rabbitInputStreamProvider = new RabbitInputStreamProvider(master.createChannel(),
                queueName);
        try {
            for (Replicable<?, ?> replicable : getReplicables()) {
                logger.info("Starting to receive initial load");
                replicable.initiallyFillFrom(new GZIPInputStream(rabbitInputStreamProvider.getInputStream()));
                logger.info("Done receiving initial load");
                replicator.setSuspended(false); // apply queued operations
            }
        } finally {
            // delete initial load queue
            DeleteOk deleteOk = consumer.getChannel().queueDelete(queueName);
            logger.info("Deleted queue " + queueName + " used for initial load: " + deleteOk.toString());
        }
    }

    /**
     * @return the UUID that the master generated for this client which is also entered into {@link #replicaUUIDs}
     */
    private String registerReplicaWithMaster(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException {
        URL replicationRegistrationRequestURL = master.getReplicationRegistrationRequestURL(getServerIdentifier(), BuildVersion.getBuildVersion());
        final URLConnection registrationRequestConnection = replicationRegistrationRequestURL.openConnection();
        registrationRequestConnection.connect();
        InputStream content = (InputStream) registrationRequestConnection.getContent();
        StringBuilder uuid = new StringBuilder();
        byte[] buf = new byte[256];
        int read = content.read(buf);
        while (read != -1) {
            uuid.append(new String(buf, 0, read));
            read = content.read(buf);
        }
        String replicaUUID = uuid.toString();
        registerReplicaUuidForMaster(replicaUUID, master);
        return replicaUUID;
    }
    
    protected void deregisterReplicaWithMaster(ReplicationMasterDescriptor master) {
        try {
            URL replicationDeRegistrationRequestURL = master.getReplicationDeRegistrationRequestURL(getServerIdentifier());
            final URLConnection deregistrationRequestConnection = replicationDeRegistrationRequestURL.openConnection();
            deregistrationRequestConnection.connect();
            StringBuilder uuid = new StringBuilder();
            InputStream content = (InputStream) deregistrationRequestConnection.getContent();
            byte[] buf = new byte[256];
            int read = content.read(buf);
            while (read != -1) {
                uuid.append(new String(buf, 0, read));
                read = content.read(buf);
            }
            content.close();
        } catch (Exception ex) {
            // ignore exceptions here - they will mostly be caused by an incompatible server
            // it is also not problematic if the server does not get this deregistration
            // a new registration will overwrite the current one
        }
    }

    protected void registerReplicaUuidForMaster(String uuid, ReplicationMasterDescriptor master) {
        replicaUUIDs.put(master, uuid);
    }

    @Override
    public Map<Class<? extends OperationWithResult<?, ?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor) {
        return replicationInstancesManager.getStatistics(replicaDescriptor);
    }
    
    @Override
    public double getAverageNumberOfOperationsPerMessage(ReplicaDescriptor replicaDescriptor) {
        return replicationInstancesManager.getAverageNumberOfOperationsPerMessage(replicaDescriptor);
    }
    
    @Override
    public long getNumberOfMessagesSent(ReplicaDescriptor replica) {
        return replicationInstancesManager.getNumberOfMessagesSent(replica);
    }
    
    @Override
    public long getNumberOfBytesSent(ReplicaDescriptor replica) {
        return replicationInstancesManager.getNumberOfBytesSent(replica);
    }
    
    @Override 
    public double getAverageNumberOfBytesPerMessage(ReplicaDescriptor replica) {
        return replicationInstancesManager.getAverageNumberOfBytesPerMessage(replica);
    }
    
    @Override
    public void stopToReplicateFromMaster() throws IOException {
        ReplicationMasterDescriptor descriptor = getReplicatingFromMaster();
        if (descriptor != null) {
            synchronized(replicaUUIDs) {
                if (replicator != null) {
                    replicator.stop();
                    deregisterReplicaWithMaster(descriptor);
                    replicatingFromMaster = null;
                    replicaUUIDs.clear();
                    
                    // this is needed because QueuingConsumer.nextDelivery() wont unblock
                    // if the connection is closed by application.
                    replicatorThread.interrupt();
                    replicator = null;
                }
            }
        }
    }

    @Override
    public void stopAllReplica() throws IOException {
        if (replicationInstancesManager.hasReplicas()) {
            replicationInstancesManager.removeAll();
            removeAsListenerFromReplicables();
            synchronized (this) {
                if (masterChannel != null) {
                    masterChannel.close();
                    masterChannel = null;
                }
            }
            logger.info("Unregistered all replicas from this server!");
        }
    }

    @Override
    public UUID getServerIdentifier() {
        return serverUUID;
    }

}
