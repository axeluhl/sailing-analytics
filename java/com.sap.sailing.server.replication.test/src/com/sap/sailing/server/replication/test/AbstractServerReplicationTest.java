package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.Activator;
import com.sap.sailing.server.replication.impl.RabbitOutputStream;
import com.sap.sailing.server.replication.impl.ReplicaDescriptor;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationMasterDescriptorImpl;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.server.replication.impl.Replicator;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBService;

public abstract class AbstractServerReplicationTest {
    protected static final int SERVLET_PORT = 9990;
    private DomainFactory resolveAgainst;
    protected RacingEventServiceImpl replica;
    protected RacingEventServiceImpl master;
    protected ReplicationServiceTestImpl replicaReplicator;
    private ReplicaDescriptor replicaDescriptor;
    private ReplicationServiceImpl<RacingEventService, RacingEventServiceOperation<?>> masterReplicator;
    private ReplicationMasterDescriptor  masterDescriptor;
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(5 * 60 * 1000); // timeout after 5 minutes
    private Thread initialLoadTestServerThread;

    /**
     * Drops the test DB. Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        try {
            Util.Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> result = basicSetUp(
                    true, /* master=null means create a new one */ null,
            /* replica=null means create a new one */null);
            result.getA().startToReplicateFrom(result.getB());
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    /**
     * Drops the test DB.
     * 
     * @param master
     *            if not <code>null</code>, the value will be used for {@link #master}; otherwise, a new racing event
     *            service will be created as master
     * @param replica
     *            if not <code>null</code>, the value will be used for {@link #replica}; otherwise, a new racing event
     *            service will be created as replica
     */
    protected Util.Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> basicSetUp(
            boolean dropDB, RacingEventServiceImpl master, RacingEventServiceImpl replica) throws IOException, InterruptedException {
        String exchangeName = "test-sapsailinganalytics-exchange";
        String exchangeHost = "localhost";
        if (System.getenv(Activator.ENV_VAR_NAME_REPLICATION_HOST) != null) {
            exchangeHost = System.getenv(Activator.ENV_VAR_NAME_REPLICATION_HOST);
        }
        final UUID serverUuid = UUID.randomUUID();
        final MongoDBService mongoDBService = MongoDBService.INSTANCE;
        if (dropDB) {
            mongoDBService.getDB().dropDatabase();
        }
        resolveAgainst = DomainFactory.INSTANCE;
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService);
        mongoObjectFactory.getDatabase().requestStart();
        if (master != null) {
            this.master = master;
        } else {
            this.master = createNewMaster(mongoDBService, mongoObjectFactory);
        }
        if (replica != null) {
            this.replica = replica;
        } else {
            this.replica = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService,
                    // replica gets its own base DomainFactory:
                    new DomainFactoryImpl()), mongoObjectFactory, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        }
        ReplicationInstancesManager<RacingEventService> rim = new ReplicationInstancesManager<>();
        masterReplicator = new ReplicationServiceImpl<RacingEventService, RacingEventServiceOperation<?>>(exchangeName, exchangeHost, rim, this.master);
        replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost(), serverUuid, "");
        masterReplicator.registerReplica(replicaDescriptor);
        // connect to exchange host and local server running as master
        // master server and exchange host can be two different hosts
        masterDescriptor = new ReplicationMasterDescriptorImpl(exchangeHost, exchangeName, 0, UUID.randomUUID().toString(), "localhost", SERVLET_PORT);
        ReplicationServiceTestImpl replicaReplicator = new ReplicationServiceTestImpl(exchangeName, exchangeHost, resolveAgainst, rim,
                replicaDescriptor, this.replica, this.master, masterReplicator, masterDescriptor);
        Util.Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> result = new Util.Pair<>(replicaReplicator, masterDescriptor);
        initialLoadTestServerThread = replicaReplicator.startInitialLoadTransmissionServlet();
        this.replicaReplicator = replicaReplicator; 
        return result;
    }

    protected RacingEventServiceImpl createNewMaster(final MongoDBService mongoDBService,
            final MongoObjectFactory mongoObjectFactory) {
        return new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService,
                DomainFactory.INSTANCE), mongoObjectFactory, MediaDBFactory.INSTANCE.getMediaDB(mongoDBService),
                EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
    }

    @After
    public void tearDown() throws Exception {
        final MongoDBService mongoDBService = MongoDBService.INSTANCE;
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoDBService);
        mongoObjectFactory.getDatabase().requestDone();
        if (masterReplicator != null) {
            masterReplicator.unregisterReplica(replicaDescriptor);
        }
        if (masterDescriptor != null) {
            masterDescriptor.stopConnection();
        }
        try {
            if (initialLoadTestServerThread != null) {
                URLConnection urlConnection = new URL("http://localhost:"+SERVLET_PORT+"/STOP").openConnection(); // stop the initial load test server thread
                urlConnection.getInputStream().close();
                initialLoadTestServerThread.join(10000 /* wait 10s */);
                assertFalse("Expected initial load test server thread to die", initialLoadTestServerThread.isAlive());
            }
        } catch (ConnectException ex) {
            /* do not make tests fail because of a server that has been shut down
             * or when an exception occured (see setUp()) - let the
             * original exception propagate */
            ex.printStackTrace();
        }
    }
    
    public void stopReplicatingToMaster() throws IOException {
        replicaReplicator.stopToReplicateFromMaster();
    }
    
    static class ReplicationServiceTestImpl extends ReplicationServiceImpl<RacingEventService, RacingEventServiceOperation<?>> {
        protected static final int INITIAL_LOAD_PACKAGE_SIZE = 1024*1024;
        private final DomainFactory resolveAgainst;
        private final RacingEventService master;
        private final ReplicaDescriptor replicaDescriptor;
        private final ReplicationService<RacingEventService> masterReplicationService;
        private final ReplicationMasterDescriptor masterDescriptor;
        
        public ReplicationServiceTestImpl(String exchangeName, String exchangeHost, DomainFactory resolveAgainst,
                ReplicationInstancesManager<RacingEventService> replicationInstancesManager, ReplicaDescriptor replicaDescriptor,
                RacingEventService replica, RacingEventService master, ReplicationService<RacingEventService> masterReplicationService,
                ReplicationMasterDescriptor masterDescriptor)
                throws IOException {
            super(exchangeName, exchangeHost, replicationInstancesManager, replica);
            this.resolveAgainst = resolveAgainst;
            this.replicaDescriptor = replicaDescriptor;
            this.master = master;
            this.masterReplicationService = masterReplicationService;
            this.masterDescriptor = masterDescriptor;
        }
        
        private Thread startInitialLoadTransmissionServlet() throws InterruptedException {
            final boolean[] listening = new boolean[] { false };
            Thread initialLoadTestServerThread = new Thread("Replication initial load test server") {
                public void run() {
                    ServerSocket ss;
                    try {
                        ss = new ServerSocket(SERVLET_PORT);
                        synchronized (listening) {
                            listening[0] = true;
                            listening.notifyAll();
                        }
                        boolean stop = false;
                        while (!stop) {
                            Socket s = ss.accept();
                            String request = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                            pw.println("HTTP/1.1 200 OK");
                            pw.println("Content-Type: text/plain");
                            pw.println();
                            pw.flush();
                            if (request.contains("DEREGISTER")) {
                                // assuming that it is safe to unregister all replicas for tests
                                for (ReplicaDescriptor descriptor : getReplicaInfo()) {
                                    unregisterReplica(descriptor);
                                }
                            } else if (request.contains("REGISTER")) {
                                final String uuid = UUID.randomUUID().toString();
                                registerReplicaUuidForMaster(uuid, masterDescriptor);
                                pw.print(uuid.getBytes());
                            } else if (request.contains("INITIAL_LOAD")) {
                                Channel channel = masterReplicationService.createMasterChannel();
                                RabbitOutputStream ros = new RabbitOutputStream(INITIAL_LOAD_PACKAGE_SIZE, channel,
                                        /* queueName */ "initial-load-for-TestClient-"+UUID.randomUUID(), /* syncAfterTimeout */ false);
                                pw.println(ros.getQueueName());
                                final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(ros);
                                final ObjectOutputStream oos = new ObjectOutputStream(gzipOutputStream);
                                master.serializeForInitialReplication(oos);
                                gzipOutputStream.finish();
                                ros.close();
                            } else if (request.contains("STOP")) {
                                stop = true;
                            }
                            pw.close();
                            s.close();
                        }
                        ss.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            initialLoadTestServerThread.start();
            synchronized (listening) {
                while (!listening[0]) {
                    listening.wait();
                }
            }
            return initialLoadTestServerThread;
        }

        /**
         * Ignore the master descriptor and replicate from the local master passed to the constructor instead.
         */
//        @Override
//        public void startToReplicateFrom(ReplicationMasterDescriptor master) throws IOException,
//                ClassNotFoundException {
//            Replicator replicator = startToReplicateFromButDontYetFetchInitialLoad(master, /* startReplicatorSuspended */ true);
//            initialLoad();
//            replicator.setSuspended(false); // resume after initial load
//        }

        protected Replicator<RacingEventService, RacingEventServiceOperation<?>> startToReplicateFromButDontYetFetchInitialLoad(ReplicationMasterDescriptor master, boolean startReplicatorSuspended)
                throws IOException {
            masterReplicationService.registerReplica(replicaDescriptor);
            registerReplicaUuidForMaster(replicaDescriptor.getUuid().toString(), master);
            QueueingConsumer consumer = master.getConsumer();
            final Replicator<RacingEventService, RacingEventServiceOperation<?>> replicator = new Replicator<RacingEventService, RacingEventServiceOperation<?>>(
                    master, this, startReplicatorSuspended, consumer);
            new Thread(replicator).start();
            return replicator;
        }
        
        /**
         * Clones the {@link #master}'s state to the {@link #replica} using
         * {@link RacingEventServiceImpl#serializeForInitialReplication(ObjectOutputStream)} and
         * {@link RacingEventServiceImpl#initiallyFillFrom(ObjectInputStream)} through a piped input/output stream.
         * @throws InterruptedException 
         */
        protected void initialLoad() throws IOException, ClassNotFoundException, InterruptedException {
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
            getReplicable().clearReplicaState();
            getReplicable().initiallyFillFrom(dis);
            dis.close();
        }
    }
}
