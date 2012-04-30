package com.sap.sailing.server.replication.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;

import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.MessageBrokerConfiguration;
import com.sap.sailing.server.replication.impl.MessageBrokerManager;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;

public class ServerReplicationTest {
    @Test
    public void testEmptyInitialLoad() throws Exception {
        DomainFactory resolveAgainst = DomainFactory.INSTANCE;
        final RacingEventService master = new RacingEventServiceImpl();
        RacingEventService replica = new RacingEventServiceImpl();
        ReplicationInstancesManager rim = new ReplicationInstancesManager();
        MessageBrokerManager brokerMgr = new MessageBrokerManager(new MessageBrokerConfiguration("local in-VM test broker",
                "vm://localhost-jms-connection", System.getProperty("java.io.tmpdir")));
        brokerMgr.startMessageBroker();
        brokerMgr.createAndStartConnection();
        ReplicationService masterReplicator = new ReplicationServiceImpl(rim, brokerMgr, master);
        ReplicaDescriptor replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost());
        masterReplicator.registerReplica(replicaDescriptor);
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        final ObjectOutputStream oos = new ObjectOutputStream(pos);
        new Thread("clone writer") {
            public void run() {
                try {
                    master.serializeForInitialReplication(oos);
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }.start();
        ObjectInputStream dis = resolveAgainst.createObjectInputStreamResolvingAgainstThisFactory(pis);
        replica.initiallyFillFrom(dis);
        dis.close();
    }
}
