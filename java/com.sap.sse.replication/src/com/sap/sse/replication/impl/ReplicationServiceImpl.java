package com.sap.sse.replication.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.util.tracker.ServiceTracker;

import com.rabbitmq.client.AMQP.Exchange;
import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Util;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicablesProvider;
import com.sap.sse.replication.ReplicablesProvider.ReplicableLifeCycleListener;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationReceiver;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.ReplicationStatus;
import com.sap.sse.util.HttpUrlConnectionHelper;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

/**
 * Manages a set of observers of {@link Replicable}, receiving notifications for the operations they perform that
 * require replication. This service provides the connectivity and central management including central keeping of
 * replication statistics for this server instance. Triggering the broadcast of an operation notified by a
 * {@link Replicable} is in the responsibility of each individual observer.
 * <p>
 * 
 * The observers are registered only when there are replicas registered. If the last replica is de-registered, the
 * service stops observing the {@link Replicable}. Operations received that require replication are sent to the
 * {@link Exchange} to which replica queues can bind, using a {@link ReplicationReceiverImpl}. By prefixing each message
 * with the {@link Object#toString()} representation of the {@link Replicable}'s {@link Replicable#getId() ID} the
 * receiver can determine to which {@link Replicable} to forward the operation. As such, this service multiplexes the
 * replication channels for potentially many {@link Replicable}s living in this server instance.
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
public class ReplicationServiceImpl implements ReplicationService, OperationsToMasterSendingQueue {
    private static final Logger logger = Logger.getLogger(ReplicationServiceImpl.class.getName());

    private final ReplicationInstancesManager replicationInstancesManager;

    private final ReplicablesProvider replicablesProvider;

    /**
     * <code>null</code>, if this instance is not currently replicating from some master; the master's descriptor
     * otherwise; note that partial replication is supported, meaning that only operations for a subset of the
     * {@link Replicable}s running on this server will be considered when received from the master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;

    /**
     * The UUIDs with which this replica is registered by the master identified by the corresponding key
     */
    private final Map<ReplicationMasterDescriptor, String> replicaUUIDs;

    /**
     * Channel used by a master server to publish replication operations; <code>null</code> in servers that don't have
     * replicas registered
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
    private ReplicationReceiverImpl replicator;

    private final Map<String, ReplicationServiceExecutionListener<?>> executionListenersByReplicableIdAsString;

    private Thread replicatorThread;

    /**
     * Used to synchronize write access and replacements of {@link #outboundBuffer}, {@link #outboundObjectBuffer} and
     * {@link #outboundBufferClasses} when the timer scoops up the messages to send. Ensure we get a unique object
     * by using a random number appended to an empty string; otherwise, string collation may collate different monitors
     * constructed from equal string literals.
     */
    private final Object outboundBufferMonitor = ""+new Random().nextDouble();

    /**
     * Sending operations as serialized Java objects using binary RabbitMQ messages comes at an overhead. To reduce the
     * overhead, several operations can be serialized into a single message. The actual serialization of the buffer
     * happens after a short duration has passed since the last sending, managed by a {@link Timer}. Writers need to
     * synchronize on {@link #outboundBufferMonitor} which protects all of {@link #outboundBuffer},
     * {@link #outboundObjectBuffer} and {@link #outboundBufferClasses} which are replaced or cleared when the timer
     * scoops up the currently buffered operations to send them out.
     */
    private ByteArrayOutputStream outboundBuffer;

    /**
     * The {@link #outboundBuffer} contains a message with serialized operations originating from a single
     * {@link Replicable} whose {@link Replicable#getId() ID} is written as its string value to the beginning of the
     * stream using {@link DataOutputStream#writeUTF(String)}. When an operation of a {@link Replicable} with a
     * different ID is to be {@link #broadcastOperation(OperationWithResult, Replicable) broadcast}, the existing
     * {@link #outboundBuffer} needs to be transmitted and a new one is started for the {@link Replicable} now wanting
     * to replicate an operation. Access is synchronized, as for {@link #outboundBuffer} using the
     * {@link #outboundBufferMonitor}.
     */
    private String outboundBufferReplicableIdAsString;

    /**
     * An object output stream that writes to {@link #outboundBuffer}. Operations are serialized into this stream until
     * the timer acquires the {@link #outboundBufferMonitor}, closes the stream and transmits the contents of
     * {@link #outboundBuffer} as a RabbitMQ message. While still holding the monitor, the timer task creates a new
     * {@link #outboundBuffer} and a new {@link #outboundObjectBuffer} wrapping the {@link #outboundBuffer}.
     */
    private ObjectOutputStream outboundObjectBuffer;

    /**
     * Remembers the classes of the operations serialized into {@link #outboundObjectBuffer}. The list of classes in
     * this list matches with the sequence of objects written to {@link #outboundObjectBuffer} as long as the
     * {@link #outboundBufferMonitor} is being held.
     */
    private List<Class<?>> outboundBufferClasses;

    /**
     * Used to schedule the sending of all operations in {@link #outboundBuffer} using the {@link #sendingTask}.
     */
    private final Timer timer;

    /**
     * Sends all operations in {@link #outboundBuffer}. When holding the monitor of {@link #outboundBuffer}, the
     * following rules hold:
     * 
     * <ul>
     * <li>if <code>null</code>, adding an operation to {@link #outboundBuffer} needs to create and assign a new timer
     * that schedules a sending task.</li>
     * <li>if not <code>null</code>, an operation added to {@link #outboundBuffer} is guaranteed to be sent by the timer
     * </li>
     * </ul>
     * 
     */
    private TimerTask sendingTask;
    
    private final UnsentOperationsSenderJob unsentOperationsSenderJob;

    /**
     * Defines for how many milliseconds the {@link #timer} will wait since the first operation has been added to an
     * empty {@link #outboundBuffer} until it carries out the actual transmission task. The longer this duration, the
     * more operations are likely to be sent per message transmitted, reducing overhead but correspondingly increasing
     * latency.
     */
    private final long TRANSMISSION_DELAY_MILLIS = 100;

    /**
     * Defines at which message size in bytes the message will be sent regardless the {@link #TRANSMISSION_DELAY_MILLIS}
     * .
     */
    private final int TRIGGER_MESSAGE_SIZE_IN_BYTES = 1024 * 1024;

    /**
     * Counts the messages sent out by this replicator
     */
    private long messageCount;

    /**
     * Before receiving the initial load from a master is started, a message queue channel is opened through which the
     * initial load data stream is received. These channels are stored here for two reasons. If a master appears as key
     * here, an initial load process receiving from that master is currently running, and no second concurrent attempt
     * to replicate from that master should be started.
     */
    private final Map<ReplicationMasterDescriptor, InitialLoadRequest> initialLoadChannels;

    /**
     * Will be set
     */
    private boolean replicationStarting;

    private static class InitialLoadRequest {
        private final Channel channelForInitialLoad;
        private final Iterable<Replicable<?, ?>> replicables;
        private final String queueName;

        public InitialLoadRequest(Channel channelForInitialLoad, Iterable<Replicable<?, ?>> replicables, String queueName) {
            super();
            this.channelForInitialLoad = channelForInitialLoad;
            this.replicables = replicables;
            this.queueName = queueName;
        }

        public Channel getChannelForInitialLoad() {
            return channelForInitialLoad;
        }

        public Iterable<Replicable<?, ?>> getReplicables() {
            return replicables;
        }

        protected String getQueueName() {
            return queueName;
        }
    }

    private class LifeCycleListener implements ReplicableLifeCycleListener {
        @Override
        public void replicableAdded(Replicable<?, ?> replicable) {
            // add a replication listener to the new replicable only if there are replicas currently registered...
            synchronized (replicationInstancesManager) {
                // .. and at least one of them wants to replicate the replicable with that ID
                if (Util.contains(replicationInstancesManager.getAllReplicableIdsAtLeastOneReplicaIsReplicating(), replicable.getId().toString())) {
                    ensureOperationExecutionListenerAndInjectResetToMasterService(replicable);
                }
            }
        }

        @Override
        public void replicableRemoved(String replicableIdAsString) {
            ReplicationServiceExecutionListener<?> listener = executionListenersByReplicableIdAsString
                    .remove(replicableIdAsString);
            if (listener != null) {
                listener.unsubscribe();
            }
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
    public ReplicationServiceImpl(String exchangeName, String exchangeHost, int exchangePort,
            final ReplicationInstancesManager replicationInstancesManager, ReplicablesProvider replicablesProvider)
            throws IOException {
        timer = new Timer("ReplicationServiceImpl timer for delayed task sending", /* isDaemon */ true);
        unsentOperationsSenderJob = new UnsentOperationsSenderJob();
        executionListenersByReplicableIdAsString = new HashMap<>();
        initialLoadChannels = new ConcurrentHashMap<>();
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

    @Override
    protected void finalize() {
        logger.info("terminating timer " + timer);
        timer.cancel();
    }

    protected ServiceTracker<Replicable<?, ?>, Replicable<?, ?>> getReplicableTracker() {
        return new ServiceTracker<Replicable<?, ?>, Replicable<?, ?>>(Activator.getDefaultContext(),
                Replicable.class.getName(), null);
    }

    protected ReplicablesProvider getReplicablesProvider() {
        return replicablesProvider;
    }

    @Override
    public ReplicationReceiver getReplicator() {
        return replicator;
    }

    private Channel createMasterChannelAndDeclareFanoutExchange() throws IOException {
        Channel result = createMasterChannel();
        result.exchangeDeclare(exchangeName, "fanout");
        logger.info("Created fanout exchange " + exchangeName + " successfully.");
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
            logger.severe("Could not connect to messaging queue on " + connectionFactory.getHost() + ":"
                    + connectionFactory.getPort() + "/" + exchangeName);
            throw ex;
        }
        logger.info("Connected to " + connectionFactory.getHost() + ":" + connectionFactory.getPort());
        return result;
    }

    private Iterable<Replicable<?, ?>> getReplicables() {
        return replicablesProvider.getReplicables();
    }
    
    private Replicable<?, ?> getReplicable(String replicableIdAsString, boolean wait) {
        return replicablesProvider.getReplicable(replicableIdAsString, wait);
    }
    
    @Override
    public void registerReplica(ReplicaDescriptor replica) throws IOException {
        synchronized (replicationInstancesManager) {
            if (!replicationInstancesManager.hasReplicas()) {
                addAsListenerToReplicables(replica.getReplicableIdsAsStrings());
                synchronized (this) {
                    if (masterChannel == null) {
                        masterChannel = createMasterChannelAndDeclareFanoutExchange();
                    }
                }
            }
            replicationInstancesManager.registerReplica(replica);
        }
        logger.info("Registered replica " + replica);
    }

    private void addAsListenerToReplicables(String[] replicableIdsAsStringForReplicablesToReplicate) {
        for (final String replicableIdAsStringForReplicableToReplicate : replicableIdsAsStringForReplicablesToReplicate) {
            Replicable<?, ?> replicable = getReplicable(replicableIdAsStringForReplicableToReplicate, /* wait */ true);
            ensureOperationExecutionListenerAndInjectResetToMasterService(replicable);
        }
    }

    /**
     * If no listener exists yet for {@code replicable}, a new one is created, registered as listener on
     * {@code replicable} and remembered in {@link #executionListenersByReplicableIdAsString}. Otherwise, this method is
     * a no-op.
     */
    private <S> void ensureOperationExecutionListenerAndInjectResetToMasterService(Replicable<S, ?> replicable) {
        if (!executionListenersByReplicableIdAsString.containsKey(replicable.getId().toString())) {
            final ReplicationServiceExecutionListener<S> listener = new ReplicationServiceExecutionListener<S>(this, replicable);
            executionListenersByReplicableIdAsString.put(replicable.getId().toString(), listener);
        }
    }

    @Override
    public ReplicaDescriptor unregisterReplica(UUID replicaUuid) throws IOException {
        logger.info("Unregistering replica with ID " + replicaUuid);
        synchronized (replicationInstancesManager) {
            final Iterable<String> oldReplicablesInReplication = replicationInstancesManager.getAllReplicableIdsAtLeastOneReplicaIsReplicating();
            final ReplicaDescriptor unregisteredReplica = replicationInstancesManager.unregisterReplica(replicaUuid);
            final Iterable<String> newReplicablesInReplication = replicationInstancesManager.getAllReplicableIdsAtLeastOneReplicaIsReplicating();
            for (final String idAsStringOfReplicableNoReplicaIsInterestedInAnymore : Util.removeAll(newReplicablesInReplication, Util.addAll(oldReplicablesInReplication, new HashSet<>()))) {
                removeAsListenerFromReplicable(idAsStringOfReplicableNoReplicaIsInterestedInAnymore);
                synchronized (this) {
                    if (masterChannel != null) {
                        masterChannel.getConnection().close();
                        masterChannel = null;
                    }
                }
            }
            return unregisteredReplica;
        }
    }
    
    @Override
    public void unregisterReplica(ReplicaDescriptor replica) throws IOException {
        logger.info("Unregistering replica " + replica);
        unregisterReplica(replica.getUuid());
    }

    private void removeAsListenerFromReplicable(String idAsStringOfReplicableNoReplicaIsInterestedInAnymore) {
        ReplicationServiceExecutionListener<?> listener = executionListenersByReplicableIdAsString.remove(idAsStringOfReplicableNoReplicaIsInterestedInAnymore);
        if (listener != null) {
            logger.info("Unsubscribed replication listener from replicable with ID "+idAsStringOfReplicableNoReplicaIsInterestedInAnymore);
            listener.unsubscribe();
        } else {
            logger.warning("Couldn't find a replication listener on replicable with ID "+idAsStringOfReplicableNoReplicaIsInterestedInAnymore);
        }
    }

    private void removeAsListenerFromReplicables() {
        for (ReplicationServiceExecutionListener<?> listener : executionListenersByReplicableIdAsString.values()) {
            listener.unsubscribe();
        }
        executionListenersByReplicableIdAsString.clear();
    }

    /**
     * Schedules a single operation for broadcast. The operation is added to {@link #outboundBuffer}, and if not already
     * scheduled, a {@link #timer} is created and scheduled to send in {@link #TRANSMISSION_DELAY_MILLIS} milliseconds.
     * 
     * @param replicable
     *            the replicable by which the operation was executed that now will be broadcast to all replicas; the
     *            {@link Replicable#getId() ID} of this replica in its string form will be used to prefix the operation
     */
    <S, O extends OperationWithResult<S, ?>> void broadcastOperation(OperationWithResult<?, ?> operation,
            Replicable<S, O> replicable) throws IOException {
        // need to write the operations one by one, making sure the ObjectOutputStream always writes
        // identical objects again if required because they may have changed state in between
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        replicable.writeOperation(operation, bos, /* close stream */true);
        final byte[] serializedOperation = bos.toByteArray();
        synchronized (outboundBufferMonitor) {
            final String replicaIdAsString = replicable.getId().toString();
            if (outboundBuffer != null && !Util.equalsWithNull(outboundBufferReplicableIdAsString, replicaIdAsString)) {
                flushBufferToRabbitMQ(); // operation from a replicable different from that for which operations are
                                         // buffered so far --> flush
            } // still holding the monitor, so no other broadcast request from a different replicable can step in
              // between
            if (outboundBuffer == null) {
                outboundBuffer = new ByteArrayOutputStream();
                outboundBufferReplicableIdAsString = replicaIdAsString;
                ObjectOutputStream compressingObjectOutputStream = createCompressingObjectOutputStream(replicaIdAsString, outboundBuffer);
                outboundObjectBuffer = compressingObjectOutputStream;
                outboundBufferClasses = new ArrayList<>();
            }
            outboundObjectBuffer.writeObject(serializedOperation);
            outboundBufferClasses.add(operation.getClass());
            if (outboundBuffer.size() > TRIGGER_MESSAGE_SIZE_IN_BYTES) {
                logger.info("Triggering replication because buffer holds " + outboundBuffer.size()
                        + " bytes which exceeds trigger size " + TRIGGER_MESSAGE_SIZE_IN_BYTES + " bytes");
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
                logger.info("Handled " + messageCount
                        + " messages for replication. Current outbound replication queue size: "
                        + outboundBufferClasses.size());
            }
        }
    }

    private static ObjectOutputStream createCompressingObjectOutputStream(final String replicaIdAsString, OutputStream streamToWrap) throws IOException {
        LZ4BlockOutputStream zipper = new LZ4BlockOutputStream(streamToWrap);
        new DataOutputStream(zipper).writeUTF(replicaIdAsString);
        ObjectOutputStream compressingObjectOutputStream = new ObjectOutputStream(zipper);
        return compressingObjectOutputStream;
    }
    
    static InputStream createUncompressingInputStream(InputStream streamToWrap) {
        return new LZ4BlockInputStream(streamToWrap);
    }

    /**
     * Obtains the monitor on {@link #outboundBufferMonitor}, copies the references to the buffers, nulls out the
     * buffers, then releases the monitor and broadcasts the buffer.
     */
    private void flushBufferToRabbitMQ() {
        logger.fine("Trying to acquire monitor");
        final byte[] bytesToSend;
        final List<Class<?>> classesOfOperationsToSend;
        final boolean doSend;
        synchronized (outboundBufferMonitor) {
            if (outboundBuffer != null) {
                logger.fine("Preparing " + outboundBufferClasses.size()
                        + " operations for sending to RabbitMQ exchange");
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
                logger.log(Level.SEVERE, "Error trying to replicate " + classesOfOperationsToSend.size()
                        + " operations", e);
            }
        }
    }

    private void broadcastOperations(byte[] bytesToSend, List<Class<?>> classesOfOperationsToSend) throws IOException {
        logger.fine("broadcasting " + classesOfOperationsToSend.size() + " operations as " + bytesToSend.length
                + " bytes");
        if (masterChannel != null) {
            logger.fine("buffer to broadcast has " + bytesToSend.length + " bytes ("
                    + (bytesToSend.length / 1024 / 1024) + "MB)");
            long startTime = System.currentTimeMillis();
            masterChannel.basicPublish(exchangeName, /* routingKey */"", /* properties */null, bytesToSend);
            logger.fine("successfully published " + bytesToSend.length + " bytes, taking "
                    + (System.currentTimeMillis() - startTime) + "ms");
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
    public void startToReplicateFrom(final ReplicationMasterDescriptor master)
            throws IOException, ClassNotFoundException, InterruptedException {
        if (initialLoadChannels.containsKey(master)) {
            logger.warning("An initial load from "+master+" is already running, replicating the following replicables: "+
                            initialLoadChannels.get(master).getReplicables()+". Not starting a second time.");
        } else {
            final Iterable<Replicable<?, ?>> replicables = master.getReplicables();
            logger.info("Starting to replicate from " + master);
            try {
                registerReplicaWithMaster(master, replicables);
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
            final URL initialLoadURL = master.getInitialLoadURL(replicables);
            logger.info("Initial load URL is " + initialLoadURL);
            // start receiving messages already now, but start in suspended mode
            replicator = new ReplicationReceiverImpl(master, replicablesProvider, /* startSuspended */true, consumer);
            // clear Replicable state here, before starting to receive and de-serialize operations which builds up
            // new state, e.g., in competitor store
            for (Replicable<?, ?> r : replicables) {
                r.clearReplicaState();
                r.setUnsentOperationToMasterSender(this);
                r.startedReplicatingFrom(master);
            }
            replicatorThread = new Thread(replicator, "Replicator receiving from " + master.getMessagingHostname() + "/"
                    + master.getExchangeName());
            replicatorThread.start();
            logger.info("Started replicator thread");
            final URLConnection initialLoadConnection = HttpUrlConnectionHelper
                    .redirectConnectionWithBearerToken(initialLoadURL, master.getBearerToken());
            final InputStream is = (InputStream) initialLoadConnection.getContent();
            final InputStreamReader queueNameReader = new InputStreamReader(is);
            final String queueName = new BufferedReader(queueNameReader).readLine();
            queueNameReader.close();
            final Channel channel = master.createChannel();
            initialLoadChannels.put(master, new InitialLoadRequest(channel, replicables, queueName));
            final RabbitInputStreamProvider rabbitInputStreamProvider = new RabbitInputStreamProvider(channel, queueName);
            try {
                final LZ4BlockInputStream uncompressingInputStream = new LZ4BlockInputStream(rabbitInputStreamProvider.getInputStream());
                for (Replicable<?, ?> replicable : replicables) { // absolutely make sure to use the same sequence of
                                                                  // replicables as for URL (bug 3015)
                    logger.info("Starting to receive initial load for " + replicable.getId());
                    try {
                        replicable.initiallyFillFrom(uncompressingInputStream);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception trying to reveice initial load for " + replicable.getId(), e);
                        throw e;
                    }
                    logger.info("Done receiving initial load for " + replicable.getId());
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Error while receiving initial load from "+master+". Cleaning up.", e);
                deregisterReplicaWithMaster(master);
                replicator.stop(/* applyQueuedMessages */ false);
                throw e;
            } finally {
                logger.info("Closing channel " + channel + "'s connection " + channel.getConnection());
                channel.getConnection().close();
                logger.info("Resuming replicator to apply queues");
                replicator.setSuspended(false); // apply queued operations
                // delete initial load queue
                DeleteOk deleteOk = consumer.getChannel().queueDelete(queueName);
                logger.info("Deleted queue " + queueName + " used for initial load: " + deleteOk.toString());
                initialLoadChannels.remove(master);
            }
        }
    }

    /**
     * @param replicables
     *            the replica is registered for these {@link Replicable}s. The master sends operations only for
     *            replicables that at least one replica has registered for. This may mean that operations are received
     *            for replicables for which no replicable was requested. Replicas shall drop such operations silently.
     * 
     * @return the UUID that the master generated for this client which is also entered into {@link #replicaUUIDs}
     */
    private String registerReplicaWithMaster(ReplicationMasterDescriptor master, Iterable<Replicable<?, ?>> replicables) throws IOException,
            ClassNotFoundException {
        URL replicationRegistrationRequestURL = master.getReplicationRegistrationRequestURL(getServerIdentifier(),
                ServerInfo.getBuildVersion());
        final URLConnection registrationRequestConnection = HttpUrlConnectionHelper
                .redirectConnectionWithBearerToken(replicationRegistrationRequestURL, master.getBearerToken());
        final InputStream content = (InputStream) registrationRequestConnection.getContent();
        final StringBuilder uuid = new StringBuilder();
        final byte[] buf = new byte[256];
        int read = content.read(buf);
        while (read != -1) {
            uuid.append(new String(buf, 0, read));
            try {
                read = content.read(buf);
            } catch (SocketException e) {
                // the connection may have been closed already; interpret this as the end of the stream
                read = -1;
            }
        }
        final String replicaUUID = uuid.toString();
        registerReplicaUuidForMaster(replicaUUID, master);
        return replicaUUID;
    }

    protected void deregisterReplicaWithMaster(ReplicationMasterDescriptor master) {
        try {
            URL replicationDeRegistrationRequestURL = master
                    .getReplicationDeRegistrationRequestURL(getServerIdentifier());
            logger.info("Unregistering replica from master "+master+" using URL "+replicationDeRegistrationRequestURL);
            final URLConnection deregistrationRequestConnection = HttpUrlConnectionHelper
                    .redirectConnectionWithBearerToken(replicationDeRegistrationRequestURL, master.getBearerToken());
            StringBuilder uuid = new StringBuilder();
            InputStream content = (InputStream) deregistrationRequestConnection.getContent();
            byte[] buf = new byte[256];
            int read = content.read(buf);
            while (read != -1) {
                uuid.append(new String(buf, 0, read));
                read = content.read(buf);
            }
            content.close();
            for (Replicable<?, ?> r : getReplicables()) {
                logger.info("Telling replicable "+r+" that it no longer replicates from master "+master);
                r.stoppedReplicatingFrom(master);
            }
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
    public Iterable<Replicable<?, ?>> getAllReplicables() {
        return getReplicablesProvider().getReplicables();
    }

    @Override
    public void stopToReplicateFromMaster() throws IOException {
        ReplicationMasterDescriptor descriptor = getReplicatingFromMaster();
        if (descriptor != null) {
            final InitialLoadRequest initialLoad = initialLoadChannels.get(descriptor);
            if (initialLoad != null) {
                try {
                    final Channel channelForInitialLoad = initialLoad.getChannelForInitialLoad();
                    // delete initial load queue
                    DeleteOk deleteOk = channelForInitialLoad.queueDelete(initialLoad.getQueueName());
                    logger.info("Deleted queue " + initialLoad.getQueueName()+ " used for initial load: " + deleteOk.toString());
                    initialLoadChannels.remove(descriptor);
                    channelForInitialLoad.getConnection().close();
                    channelForInitialLoad.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error trying to close initial load channel / connection to message queueing system", e);
                    // let's continue trying to de-register the replica from master; re-throwing the
                    // exception wouldn't make much sense here; rather try to clean up as much as
                    // possible
                }
            }
            synchronized (replicaUUIDs) {
                if (replicator != null) {
                    replicator.stop(/* applyQueuedMessages */ true);
                    deregisterReplicaWithMaster(descriptor);
                    replicatingFromMaster = null;
                    replicaUUIDs.clear();

                    // this is needed because QueuingConsumer.nextDelivery() won't unblock
                    // if the connection is closed by application.
                    replicatorThread.interrupt();
                    replicator = null;
                }
            }
        }
    }

    @Override
    public void stopAllReplicas() throws IOException {
        if (replicationInstancesManager.hasReplicas()) {
            replicationInstancesManager.removeAll();
            removeAsListenerFromReplicables();
            synchronized (this) {
                if (masterChannel != null) {
                    masterChannel.getConnection().close();
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

    @Override
    public ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname,
            String exchangeName, int servletPort, int messagingPort, String queueName, String bearerToken,
            Iterable<Replicable<?, ?>> replicables) {
        return new ReplicationMasterDescriptorImpl(messagingHostname, exchangeName, messagingPort, queueName, hostname,
                servletPort, bearerToken, replicables);
    }

    @Override
    public void setReplicationStarting(boolean b) {
        this.replicationStarting = b;
    }
    
    @Override
    public boolean isReplicationStarting() {
        return this.replicationStarting;
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        unsentOperationsSenderJob.scheduleForSending(operationWithResult, sender);
    }

    @Override
    public synchronized ReplicationStatus getStatus() {
        final ReplicationReceiver replicationReceiver = getReplicator();
        final boolean isReplicationStarting = isReplicationStarting();
        final boolean isReplica = isReplicationStarting || replicationReceiver != null;
        final boolean suspended = replicationReceiver == null ? false : replicationReceiver.isSuspended();
        final boolean stopped = replicationReceiver == null ? false : replicationReceiver.isBeingStopped();
        long messageQueueLength;
        if (replicationReceiver == null) {
            messageQueueLength = 0;
        } else {
            try {
                messageQueueLength = replicationReceiver.getMessageQueueSize();
            } catch (IllegalAccessException e) {
                logger.warning("Unable to access replication message queue size: "+e.getMessage()+". Reporting as -1");
                messageQueueLength = -1;
            }
        }
        final Map<String, Integer> operationQueueLengths = replicationReceiver == null ? new HashMap<>() : replicationReceiver.getOperationQueueSizes();
        final Map<String, Boolean> isInitialLoadRunning = new HashMap<>();
        for (final Replicable<?, ?> replicable : getAllReplicables()) {
            isInitialLoadRunning.put(replicable.getId().toString(), replicable.isCurrentlyFillingFromInitialLoad());
        }
        return new ReplicationStatusImpl(isReplica, isReplicationStarting, suspended, stopped, messageQueueLength, isInitialLoadRunning, operationQueueLengths);
    }
}
