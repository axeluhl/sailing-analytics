package com.sap.sailing.server.replication.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.MessageBrokerConfiguration;
import com.sap.sailing.server.replication.impl.MessageBrokerManager;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.server.replication.impl.Replicator;

public abstract class AbstractServerReplicationTest {
    private DomainFactory resolveAgainst;
    protected RacingEventServiceImpl replica;
    protected RacingEventServiceImpl master;
    private MessageBrokerManager brokerMgr;
    private File brokerPersistenceDir;
    
    /**
     * Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        final MongoDBService mongoDBService = MongoDBService.INSTANCE;
        mongoDBService.getDB().dropDatabase();
        resolveAgainst = DomainFactory.INSTANCE;
        master = new RacingEventServiceImpl(mongoDBService);
        replica = new RacingEventServiceImpl(mongoDBService);
        ReplicationInstancesManager rim = new ReplicationInstancesManager();
        final String IN_VM_BROKER_URL = "vm://localhost-jms-connection";
        final String activeMQPersistenceParentDir = System.getProperty("java.io.tmpdir");
        final String brokerName = "local_in-VM_test_broker";
        brokerPersistenceDir = new File(activeMQPersistenceParentDir, brokerName);
        removeTemporaryTestBrokerPersistenceDirectory();
        brokerMgr = new MessageBrokerManager(new MessageBrokerConfiguration(brokerName,
                IN_VM_BROKER_URL, activeMQPersistenceParentDir));
        brokerMgr.startMessageBroker(/* useJmx */ false);
        brokerMgr.createAndStartConnection();
        ReplicationService masterReplicator = new ReplicationServiceImpl(rim, brokerMgr, master);
        ReplicaDescriptor replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost());
        masterReplicator.registerReplica(replicaDescriptor);
        ReplicationMasterDescriptor masterDescriptor = new ReplicationMasterDescriptor() {
            @Override
            public URL getReplicationRegistrationRequestURL() throws MalformedURLException {
                throw new UnsupportedOperationException();
            }
            @Override
            public URL getInitialLoadURL() throws MalformedURLException {
                throw new UnsupportedOperationException();
            }

            @Override
            public TopicSubscriber getTopicSubscriber(String clientID) throws JMSException, UnknownHostException {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                        ActiveMQConnection.DEFAULT_PASSWORD, IN_VM_BROKER_URL);
                connectionFactory.setClientID(clientID);
                Connection connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(ReplicationService.SAILING_SERVER_REPLICATION_TOPIC);
                return session.createDurableSubscriber(topic, InetAddress.getLocalHost().getHostAddress());
            }
        };
        ReplicationService replicaReplicator = new ReplicationServiceTestImpl(resolveAgainst, rim, brokerMgr, replicaDescriptor, replica, master, masterReplicator);
        replicaReplicator.startToReplicateFrom(masterDescriptor);
    }

    private void removeTemporaryTestBrokerPersistenceDirectory() throws FileNotFoundException {
        if (brokerPersistenceDir.exists() && brokerPersistenceDir.isDirectory()) {
            System.out.println("deleted brokerPersistenceDir: "+deleteRecursive(brokerPersistenceDir));
        }
        File failoverStore = new File("activemq-data");
        if (failoverStore.exists() && failoverStore.isDirectory()) {
            System.out.println("deleted failover store: "+deleteRecursive(failoverStore));
        }
    }
    
    private boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    @After
    public void tearDown() throws Exception {
        brokerMgr.closeSessions();
        brokerMgr.closeConnections();
        brokerMgr.stopMessageBroker();
        removeTemporaryTestBrokerPersistenceDirectory();
    }

    private static class ReplicationServiceTestImpl extends ReplicationServiceImpl {
        private final DomainFactory resolveAgainst;
        private final RacingEventService master;
        private final ReplicaDescriptor replicaDescriptor;
        private final ReplicationService masterReplicationService;
        
        public ReplicationServiceTestImpl(DomainFactory resolveAgainst,
                ReplicationInstancesManager replicationInstancesManager, MessageBrokerManager messageBrokerManager,
                ReplicaDescriptor replicaDescriptor, RacingEventService replica, RacingEventService master, ReplicationService masterReplicationService) {
            super(replicationInstancesManager, messageBrokerManager, replica);
            this.resolveAgainst = resolveAgainst;
            this.replicaDescriptor = replicaDescriptor;
            this.master = master;
            this.masterReplicationService = masterReplicationService;
        }
        
        /**
         * Ignore the master descriptor and replicate from the local master passed to the constructor instead.
         */
        @Override
        public void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException,
                ClassNotFoundException, JMSException {
            masterReplicationService.registerReplica(replicaDescriptor);
            registerReplicaUuidForMaster(replicaDescriptor.getUuid().toString(), master);
            TopicSubscriber replicationSubscription = master.getTopicSubscriber(replicaDescriptor.getUuid().toString());
            replicationSubscription.setMessageListener(new Replicator(master, this));
            initialLoad();
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
            getRacingEventService().initiallyFillFrom(dis);
            dis.close();
        }
    }
}
