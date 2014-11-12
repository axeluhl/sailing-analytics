package com.sap.sse.replication;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.sap.sse.replication.impl.ReplicaDescriptor;
import com.sap.sse.replication.impl.ReplicationServlet;

/**
 * A service that organizes a master server and its replicas.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the type representing the master's state
 */
public interface ReplicationService<S> {
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
     * Performs a servlet request to the remote master server's {@link ReplicationServlet}, first registering this
     * replica, ensuring the message queue for replicating operations is created, then subscribing to the master's
     * replication message queue and asking the servlet for the stream containing the initial load which will then be
     * used to {@link Replicable#initiallyFillFrom(java.io.ObjectInputStream) replace} the current {@link Replicable}'s
     * state.
     */
    void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, InterruptedException;

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
     * For a replica replicating off this master, provides statistics in the form of number of operations sent to that
     * replica by type, where the operation type is the key, represented as the operation's class name
     */
    Map<Class<? extends OperationWithResult<S, ?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor);
    
    double getAverageNumberOfOperationsPerMessage(ReplicaDescriptor replicaDescriptor);

    /**
     * For this instance running on a replica, stops the currently running replication. As a replica has exactly one
     * master server that it replicates, this method needs no parameters.
     */
    void stopToReplicateFromMaster() throws IOException;

    /**
     * Stops all replica currently registered with this server.
     */
    void stopAllReplica() throws IOException;
    
    /**
     * Returns an unique server identifier
     */
    UUID getServerIdentifier();

    Channel createMasterChannel() throws IOException, ConnectException;

    long getNumberOfMessagesSent(ReplicaDescriptor replica);

    long getNumberOfBytesSent(ReplicaDescriptor replica);

    double getAverageNumberOfBytesPerMessage(ReplicaDescriptor replica);
}
