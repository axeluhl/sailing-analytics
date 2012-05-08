package com.sap.sailing.server.replication;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.sap.sailing.server.replication.impl.ReplicationServlet;

public interface ReplicationService {
    static String SAILING_SERVER_REPLICATION_TOPIC = "SailingServerReplicationTopic";

    List<String> getHostnamesOfReplica();
    
    /**
     * Performs a servlet request to the server's {@link ReplicationServlet}, first registering this replica, ensuring
     * the JMS replication topic is created, then subscribing for the master's JMS replication topic and asking the servlet
     * for the stream containing the initial load. 
     */
    void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException, ClassNotFoundException, JMSException;

    /**
     * Registers a replica with this master instance. If the replication topic hasn't been created in the
     * JMS message broker yet, it will be when this method returns. The <code>replica</code> will be considered
     * in the result of {@link #getHostnamesOfReplica()} when this call has succeeded.
     */
    void registerReplica(ReplicaDescriptor replica) throws JMSException;

    void unregisterReplica(ReplicaDescriptor replica) throws JMSException;
}
