package com.sap.sse.replication;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Channel;

/**
 * A service that organizes a master server and its replicas.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the type representing the master's state
 */
public interface ReplicationService {
    static String SAILING_SERVER_REPLICATION_TOPIC = "SailingServerReplicationTopic";

    /**
     * Tells about replicas registered with this master.
     */
    Iterable<ReplicaDescriptor> getReplicaInfo();
    
    /**
     * If this server instance is a replica of some other master server instance, this method returns that master's
     * descriptor. Otherwise (if this instance is not a replica), returns <code>null</code>.
     */
    ReplicationMasterDescriptor getReplicatingFromMaster();
    
    /**
     * Starts to replicate the {@link Replicable}s that the {@code master} descriptor
     * {@link ReplicationMasterDescriptor#getReplicables() specifies}. Performs a servlet request to the remote master
     * server's {@link ReplicationServlet}, first registering this replica, ensuring the message queue for replicating
     * operations is created, then subscribing to the master's replication message queue and asking the servlet for the
     * stream containing the initial load for the specific {@link Replicables} which will then be used to
     * {@link Replicable#initiallyFillFrom(java.io.ObjectInputStream) replace} those {@link Replicable replicables}'
     * state.
     * <p>
     * 
     * Note that calling this method is not a good idea during the OSGi start-up phase, particularly if there is a
     * bundle that starts after this bundle because then that {@link Replicable} will not yet be registered and won't
     * become part of replication.
     */
    void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException,
            ClassNotFoundException, InterruptedException;

    /**
     * Registers a replica with this master instance. The <code>replica</code> will be considered in the result of
     * {@link #getReplicaInfo()} when this call has succeeded.
     */
    void registerReplica(ReplicaDescriptor replica) throws IOException;

    /**
     * When this service runs on a master instance, the <code>replica</code> will no longer be considered part of this
     * master's replica set. In particular, if this was the last replica that got de-registered, this replication
     * service will stop to pump replication operations into the message queue until a replica is
     * {@link #registerReplica(ReplicaDescriptor) registered} again.
     */
    void unregisterReplica(ReplicaDescriptor replica) throws IOException;
    
    /**
     * Same as {@link #unregisterReplica(ReplicaDescriptor)}, identifying the replica by its {@link ReplicaDescriptor#getUuid() ID}.
     */
    ReplicaDescriptor unregisterReplica(UUID replicaId) throws IOException;

    /**
     * For a replica replicating off this master, provides statistics in the form of number of operations sent to that
     * replica by type, where the operation type is the key, represented as the operation's class name
     */
    Map<Class<? extends OperationWithResult<?, ?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor);
    
    double getAverageNumberOfOperationsPerMessage(ReplicaDescriptor replicaDescriptor);

    /**
     * For this instance running on a replica, stops the currently running replication. As a replica has exactly one
     * master server that it replicates, this method needs no parameters.
     */
    void stopToReplicateFromMaster() throws IOException;

    /**
     * Stops all replica currently registered with this server.
     */
    void stopAllReplicas() throws IOException;
    
    /**
     * Returns an unique server identifier
     */
    UUID getServerIdentifier();

    Channel createMasterChannel() throws IOException, ConnectException;

    long getNumberOfMessagesSent(ReplicaDescriptor replica);

    long getNumberOfBytesSent(ReplicaDescriptor replica);

    double getAverageNumberOfBytesPerMessage(ReplicaDescriptor replica);

    Iterable<Replicable<?, ?>> getAllReplicables();

    ReplicationMasterDescriptor createReplicationMasterDescriptor(String messagingHostname, String hostname, String exchangeName, int servletPort, int jmsPort, String jmsQueueName, Iterable<Replicable<?, ?>> replicables);

    /**
     * @return a replication receiver that is responsible for receiving replicated operations from a message queue and
     *         for dispatching those operations to their respective {@link Replicable}s; or {@code null} in case
     *         currently no replication is active. Note, though, that this service may already be aware that it will
     *         start replicating any time soon but hasn't actually done so. This can be seen from
     *         {@link #isReplicationStarting()}. Therefore, a reasonable availability / health check shall check the
     *         result of this method, the result of {@link #isReplicationStarting()} as well as the
     *         {@link Replicable#isCurrentlyFillingFromInitialLoad()} results.
     */
    ReplicationReceiver getReplicator();

    void setReplicationStarting(boolean b);

    /**
     * @return {@code true} if replication is starting; during this phase it is clear that the replicables managed by
     *         this service instance are about to be {@link Replicable#clearReplicaState() cleared} and to be initialized
     *         with an {@link Replicable#initiallyFillFrom(java.io.InputStream) initial load} obtained from the master
     *         instance. However, this initial load process may not yet have started and hence the {@link #getReplicator} method
     *         
     */
    boolean isReplicationStarting();
    
    /**
     * Provides a compound overview of this service and the {@link Replicable}s it manages.
     * This can be used in compound server availability checks, for example.
     */
    ReplicationStatus getStatus();
}
