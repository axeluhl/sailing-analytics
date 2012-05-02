package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.HasRacingEventService;
import com.sap.sailing.server.replication.impl.MessageBrokerConfiguration;
import com.sap.sailing.server.replication.impl.MessageBrokerManager;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationMasterDescriptorImpl;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.server.replication.impl.Replicator;

public class ServerReplicationTest {
    private DomainFactory resolveAgainst;
    private RacingEventServiceImpl replica;
    private RacingEventServiceImpl master;

    /**
     * Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        resolveAgainst = DomainFactory.INSTANCE;
        master = new RacingEventServiceImpl();
        replica = new RacingEventServiceImpl();
        ReplicationInstancesManager rim = new ReplicationInstancesManager();
        MessageBrokerManager brokerMgr = new MessageBrokerManager(new MessageBrokerConfiguration("local in-VM test broker",
                "vm://localhost-jms-connection", System.getProperty("java.io.tmpdir")));
        brokerMgr.startMessageBroker();
        brokerMgr.createAndStartConnection();
        ReplicationService masterReplicator = new ReplicationServiceImpl(rim, brokerMgr, master);
        ReplicaDescriptor replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost());
        masterReplicator.registerReplica(replicaDescriptor);
        ReplicationMasterDescriptorImpl masterDescriptor = new ReplicationMasterDescriptorImpl("localhost", 8888, 61616);
        ReplicationService replicaReplicator = new ReplicationServiceImpl(rim, brokerMgr, replica);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD, "vm://localhost-jms-connection");
        connectionFactory.setClientID("Test Client");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(ReplicationService.SAILING_SERVER_REPLICATION_TOPIC);
        TopicSubscriber subscriber = session.createDurableSubscriber(topic, InetAddress.getLocalHost().getHostAddress());
        subscriber.setMessageListener(createReplicator(masterDescriptor, replica));
        replicaReplicator.startToReplicateFrom(masterDescriptor);
    }

    private Replicator createReplicator(ReplicationMasterDescriptorImpl masterDescriptor, final RacingEventService master) {
        return new Replicator(masterDescriptor, new HasRacingEventService() {
            @Override
            public RacingEventService getRacingEventService() {
                return master;
            }
        });
    }
    
    @Test
    public void testBasicInitialLoad() throws Exception {
        initialLoad();
        assertEquals(Util.size(master.getAllEvents()), Util.size(replica.getAllEvents()));
        assertEquals(master.getLeaderboardGroups().size(), replica.getLeaderboardGroups().size());
        assertEquals(master.getLeaderboards().size(), replica.getLeaderboards().size());
        assertEquals(master.getLeaderboards().keySet(), replica.getLeaderboards().keySet());
    }

    /**
     * Clones the {@link #master}'s state to the {@link #replica} using
     * {@link RacingEventServiceImpl#serializeForInitialReplication(ObjectOutputStream)} and
     * {@link RacingEventServiceImpl#initiallyFillFrom(ObjectInputStream)} through a piped input/output stream.
     */
    private void initialLoad() throws IOException, ClassNotFoundException {
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
