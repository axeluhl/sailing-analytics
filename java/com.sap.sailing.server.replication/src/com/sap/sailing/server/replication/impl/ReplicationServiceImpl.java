package com.sap.sailing.server.replication.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.osgi.util.tracker.ServiceTracker;

import com.rabbitmq.client.AMQP.Exchange;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sailing.server.OperationExecutionListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.util.BuildVersion;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Can observe a {@link RacingEventService} for the operations it performs that require replication. Only observes as
 * long as there are replicas registered. If the last replica is de-registered, the service stops observing the
 * {@link RacingEventService}. Operations received that require replication are sent to the {@link Exchange} to which
 * replica queues can bind. The exchange name is provided to this service during construction.
 * <p>
 * 
 * This service object {@link RacingEventService#addOperationExecutionListener(OperationExecutionListener) registers} as
 * listener at the {@link RacingEventService} so it {@link #executed(RacingEventServiceOperation) receives}
 * notifications about operations executed by the {@link RacingEventService} that require replication if and only if
 * there is at least one replica registered.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class ReplicationServiceImpl implements ReplicationService, OperationExecutionListener, HasRacingEventService {
    private static final Logger logger = Logger.getLogger(ReplicationServiceImpl.class.getName());
    
    private final ReplicationInstancesManager replicationInstancesManager;
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final RacingEventService localService;
    
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
    private final String exchangeHost;
    
    /**
     * UUID that identifies this server
     */
    private final UUID serverUUID;
    
    private Replicator replicator;
    private Thread replicatorThread;
    
    /**
     * Sending operations as serialized Java objects using binary RabbitMQ messages comes at an overhead. To reduce the overhead,
     * several operations can be serialized into a single message. The actual serialization of the buffer happens after a short duration
     * has passed since the last sending, managed by a {@link Timer}. Writers need to synchronize on this buffer. This includes the
     * addition of an operation to the buffer as well as the atomic sending and clearing.
     */
    private final List<Pair<Class<?>, byte[]>> outboundBuffer;
    
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
     * Counts the messages sent out by this replicator
     */
    private long messageCount;
    
    public ReplicationServiceImpl(String exchangeName, String exchangeHost, final ReplicationInstancesManager replicationInstancesManager) throws IOException {
        this(exchangeName, exchangeHost, replicationInstancesManager, /* localService */ null, /* create RacingEventServiceTracker */ true);
    }
    
    private ReplicationServiceImpl(String exchangeName, String exchangeHost,
            final ReplicationInstancesManager replicationInstancesManager, RacingEventService localService,
            boolean createRacingEventServiceTracker) throws IOException {
        timer = new Timer("ReplicationServiceImpl timer for delayed task sending");
        this.replicationInstancesManager = replicationInstancesManager;
        replicaUUIDs = new HashMap<ReplicationMasterDescriptor, String>();
        if (createRacingEventServiceTracker) {
            racingEventServiceTracker = getRacingEventServiceTracker();
            racingEventServiceTracker.open();
        } else {
            racingEventServiceTracker = null;
        }
        this.localService = localService;
        this.exchangeName = exchangeName;
        this.exchangeHost = exchangeHost;
        replicator = null;
        serverUUID = UUID.randomUUID();
        outboundBuffer = new ArrayList<>();
        logger.info("Setting " + serverUUID.toString() + " as unique replication identifier.");
        
    }

    /**
     * Like {@link #ReplicationServiceImpl(String, ReplicationInstancesManager)}, only that instead of using an OSGi
     * service tracker to discover the {@link RacingEventService}, the service to replicate is "injected" here.
     * 
     * @param exchangeName
     *            the name of the exchange to which replicas can bind
     */
    public ReplicationServiceImpl(String exchangeName, String exchangeHost,
            final ReplicationInstancesManager replicationInstancesManager, RacingEventService localService) throws IOException {
        this(exchangeName, exchangeHost, replicationInstancesManager, localService, /* create RacingEventServiceTracker */ false);
    }
    
    protected ServiceTracker<RacingEventService, RacingEventService> getRacingEventServiceTracker() {
        return new ServiceTracker<RacingEventService, RacingEventService>(
                Activator.getDefaultContext(), RacingEventService.class.getName(), null);
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
        connectionFactory.setHost(exchangeHost); // ...and use default port
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

    @Override
    public RacingEventService getRacingEventService() {
        RacingEventService result;
        if (localService != null) {
            result = localService;
        } else {
            result = racingEventServiceTracker.getService();
        }
        return result;
    }

    @Override
    public void registerReplica(ReplicaDescriptor replica) throws IOException {
        if (!replicationInstancesManager.hasReplicas()) {
            addAsListenerToRacingEventService();
            synchronized (this) {
                if (masterChannel == null) {
                    masterChannel = createMasterChannelAndDeclareFanoutExchange();
                }
            }
        }
        replicationInstancesManager.registerReplica(replica);
        logger.info("Registered replica " + replica);
    }
    
    private void addAsListenerToRacingEventService() {
        getRacingEventService().addOperationExecutionListener(this);
    }

    @Override
    public void unregisterReplica(ReplicaDescriptor replica) throws IOException {
        logger.info("Unregistering replica " + replica);
        replicationInstancesManager.unregisterReplica(replica);
        if (!replicationInstancesManager.hasReplicas()) {
            removeAsListenerFromRacingEventService();
            synchronized (this) {
                if (masterChannel != null) {
                    masterChannel.close();
                    masterChannel = null;
                }
            }
        }
    }

    private void removeAsListenerFromRacingEventService() {
        getRacingEventService().removeOperationExecutionListener(this);
    }

    /**
     * Schedules a single operation for broadcast. The operation is added to {@link #outboundBuffer}, and if not already scheduled,
     * a {@link #timer} is created and scheduled to send in {@link #TRANSMISSION_DELAY_MILLIS} milliseconds.
     */
    private void broadcastOperation(RacingEventServiceOperation<?> operation) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(operation);
        oos.close();
        final byte[] bytes = bos.toByteArray();
        synchronized (outboundBuffer) {
            outboundBuffer.add(new Pair<Class<?>, byte[]>(operation.getClass(), bytes));
            if (sendingTask == null) {
                sendingTask = new TimerTask() {
                    @Override
                    public void run() {
                        logger.fine("Running timer task to send "+outboundBuffer.size()+" messages");
                        final Iterable<Pair<Class<?>, byte[]>> listToSend;
                        synchronized (outboundBuffer) {
                            logger.fine("Copying "+outboundBuffer.size()+" messages to new list");
                            listToSend = new ArrayList<>(outboundBuffer);
                            outboundBuffer.clear();
                            sendingTask = null;
                        }
                        try {
                            broadcastOperations(listToSend);
                            logger.fine("Successfully handed "+Util.size(listToSend)+" messages to broadcaster");
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error trying to replicate the following operations: "+listToSend, e);
                        }
                    }
                };
                timer.schedule(sendingTask, TRANSMISSION_DELAY_MILLIS);
            }
            if (++messageCount % 10000l == 0) {
                logger.info("Handled "+messageCount+" messages for replication. Current outbound replication queue size: "+outboundBuffer.size());
            }
        }
    }
    
    private void broadcastOperations(Iterable<Pair<Class<?>, byte[]>> listToSend) throws IOException {
        logger.fine("broadcasting "+Util.size(listToSend)+" operations");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buf);
        List<Class<?>> classes = new ArrayList<>();
        List<byte[]> byteArrays = new ArrayList<>();
        for (Pair<Class<?>, byte[]> op : listToSend) {
            classes.add(op.getA());
            byteArrays.add(op.getB());
        }
        oos.writeObject(byteArrays);
        oos.close();
        // copy serialized operations into message
        if (masterChannel != null) {
            final int queueMessageSize = buf.size();
            logger.fine("buffer to broadcast has "+queueMessageSize+" bytes ("+(queueMessageSize/1024/1024)+"MB)");
            masterChannel.basicPublish(exchangeName, /* routingKey */"", /* properties */null, buf.toByteArray());
            logger.fine("successfully published "+queueMessageSize+" bytes");
            replicationInstancesManager.log(classes,queueMessageSize);
        }
    }

    @Override
    public Iterable<ReplicaDescriptor> getReplicaInfo() {
        return replicationInstancesManager.getReplicaDescriptors();
    }

    @Override
    public ReplicationMasterDescriptor isReplicatingFromMaster() {
        return replicatingFromMaster;
    }

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
        URL initialLoadURL = master.getInitialLoadURL();
        logger.info("Initial load URL is "+initialLoadURL);
        replicator = new Replicator(master, this, /* startSuspended */ true, consumer, getRacingEventService().getBaseDomainFactory());
        // start receiving messages already now, but start in suspended mode
        replicatorThread = new Thread(replicator, "Replicator receiving from "+master.getHostname()+"/"+master.getExchangeName());
        replicatorThread.start();
        logger.info("Started replicator thread");
        InputStream is = initialLoadURL.openStream();
        String queueName = new BufferedReader(new InputStreamReader(is)).readLine();
        RabbitInputStreamProvider rabbitInputStreamProvider = new RabbitInputStreamProvider(master.createChannel(), queueName);
        final RacingEventService racingEventService = getRacingEventService();
        ObjectInputStream ois = racingEventService.getBaseDomainFactory().createObjectInputStreamResolvingAgainstThisFactory(new GZIPInputStream(
                rabbitInputStreamProvider.getInputStream()));
        logger.info("Starting to receive initial load");
        racingEventService.initiallyFillFrom(ois);
        logger.info("Done receiving initial load");
        replicator.setSuspended(false); // apply queued operations
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

    /**
     * {@link #broadcastOperation(RacingEventServiceOperation) Broadcasts} the <code>operation</code> to all registered
     * replicas by publishing it to the fan-out exchange.
     */
    @Override
    public <T> void executed(RacingEventServiceOperation<T> operation) {
        try {
            broadcastOperation(operation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerReplicaUuidForMaster(String uuid, ReplicationMasterDescriptor master) {
        replicaUUIDs.put(master, uuid);
    }

    @Override
    public Map<Class<? extends RacingEventServiceOperation<?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor) {
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
        ReplicationMasterDescriptor descriptor = isReplicatingFromMaster();
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
            removeAsListenerFromRacingEventService();
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
