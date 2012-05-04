package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.EventName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddEvent;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboard;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.MessageBrokerConfiguration;
import com.sap.sailing.server.replication.impl.MessageBrokerManager;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.server.replication.impl.Replicator;

public class ServerReplicationTest {
    private DomainFactory resolveAgainst;
    private RacingEventServiceImpl replica;
    private RacingEventServiceImpl master;
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

    @Test
    public void testBasicInitialLoad() throws Exception {
        assertNotSame(master, replica);
        assertEquals(Util.size(master.getAllEvents()), Util.size(replica.getAllEvents()));
        assertEquals(master.getLeaderboardGroups().size(), replica.getLeaderboardGroups().size());
        assertEquals(master.getLeaderboards().size(), replica.getLeaderboards().size());
        assertEquals(master.getLeaderboards().keySet(), replica.getLeaderboards().keySet());
    }
    
    @Test
    public void testLeaderboardCreationReplication() throws InterruptedException {
        Thread.sleep(1000); // wait 1s for JMS to deliver any recovered messages; there should be none
        final String leaderboardName = "My new leaderboard";
        assertNull(replica.getLeaderboardByName(leaderboardName));
        final int[] discardThresholds = new int[] { 17, 23 };
        CreateLeaderboard createTestLeaderboard = new CreateLeaderboard(leaderboardName, discardThresholds);
        assertNull(master.getLeaderboardByName(leaderboardName));
        master.apply(createTestLeaderboard);
        final Leaderboard masterLeaderboard = master.getLeaderboardByName(leaderboardName);
        assertNotNull(masterLeaderboard);
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        final Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNotNull(replicaLeaderboard);
        assertTrue(Arrays.equals(masterLeaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(),
                replicaLeaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces()));
    }

    @Test
    public void testLeaderboardRemovalReplication() throws InterruptedException {
        final String leaderboardName = DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME;
        assertNotNull(replica.getLeaderboardByName(leaderboardName));
        assertNotNull(master.getLeaderboardByName(leaderboardName));
        RemoveLeaderboard removeDefaultLeaderboard = new RemoveLeaderboard(leaderboardName);
        master.apply(removeDefaultLeaderboard);
        final Leaderboard masterLeaderboard = master.getLeaderboardByName(leaderboardName);
        assertNull(masterLeaderboard);
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        final Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNull(replicaLeaderboard);
    }
    
    @Test
    public void testWaypointRemovalReplication() throws InterruptedException {
        final String boatClassName = "49er";
        // FIXME use master DomainFactory
        final DomainFactory masterDomainFactory = DomainFactory.INSTANCE;
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName, /* typicallyStartsUpwind */ true);
        final String baseEventName = "Test Event";
        AddEvent addEventOperation = new AddEvent(baseEventName, boatClassName, /* boatClassTypicallyStartsUpwind */ true);
        Event event = master.apply(addEventOperation);
        final String raceName = "Test Race";
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        RaceDefinition race = new RaceDefinitionImpl(raceName, masterCourse, boatClass,
                new ArrayList<Competitor>());
        AddRaceDefinition addRaceOperation = new AddRaceDefinition(new EventName(event.getName()), race);
        master.apply(addRaceOperation);
        masterCourse.addWaypoint(0, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateBuoy("Buoy1")));
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        Event replicaEvent = replica.getEvent(new EventName(event.getName()));
        assertNotNull(replicaEvent);
        RaceDefinition replicaRace = replicaEvent.getRaceByName(raceName);
        assertNotNull(replicaRace);
        Course replicaCourse = replicaRace.getCourse();
        assertEquals(1, Util.size(replicaCourse.getWaypoints()));
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
