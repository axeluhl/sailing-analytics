package com.sap.sse.server.replication;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.sap.sse.server.replication.impl.ReplicaDescriptor;
import com.sap.sse.server.replication.impl.ReplicationServlet;

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
    ReplicationMasterDescriptor isReplicatingFromMaster();
    
    /**
     * Performs a servlet request to the server's {@link ReplicationServlet}, first registering this replica, ensuring
     * the JMS replication topic is created, then subscribing for the master's JMS replication topic and asking the servlet
     * for the stream containing the initial load. 
     * @throws InterruptedException 
     */
    void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     * Registers a replica with this master instance. The <code>replica</code> will be considered in the result of
     * {@link #getReplicaInfo()} when this call has succeeded.
     */
    void registerReplica(ReplicaDescriptor replica) throws IOException;

    void unregisterReplica(ReplicaDescriptor replica) throws IOException;

    /**
     * For a replica replicating off this master, provides statistics in the form of number of operations sent to that
     * replica by type, where the operation type is the key, represented as the operation's class name
     */
    Map<Class<? extends OperationWithResult<S, ?>>, Integer> getStatistics(ReplicaDescriptor replicaDescriptor);
    
    double getAverageNumberOfOperationsPerMessage(ReplicaDescriptor replicaDescriptor);

    /**
     * Stops the currently running replication. As there can be only one replication running
     * this method needs no parameters.
     * @throws IOException 
     */
    void stopToReplicateFromMaster() throws IOException;

    /**
     * Stops all replica currently registered with this server.
     * @throws IOException 
     */
    void stopAllReplica() throws IOException;
    
    /**
     * Returns an unique server identifier
     * @return
     */
    UUID getServerIdentifier();

    Channel createMasterChannel() throws IOException, ConnectException;

    long getNumberOfMessagesSent(ReplicaDescriptor replica);

    long getNumberOfBytesSent(ReplicaDescriptor replica);

    double getAverageNumberOfBytesPerMessage(ReplicaDescriptor replica);
}
