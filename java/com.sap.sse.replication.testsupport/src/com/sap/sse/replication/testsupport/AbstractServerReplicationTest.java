package com.sap.sse.replication.testsupport;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.impl.Activator;
import com.sap.sse.replication.impl.RabbitOutputStream;
import com.sap.sse.replication.impl.ReplicaDescriptor;
import com.sap.sse.replication.impl.ReplicationInstancesManager;
import com.sap.sse.replication.impl.ReplicationMasterDescriptorImpl;
import com.sap.sse.replication.impl.ReplicationReceiver;
import com.sap.sse.replication.impl.ReplicationServiceImpl;
import com.sap.sse.replication.impl.SingletonReplicablesProvider;

public abstract class AbstractServerReplicationTest<ReplicableInterface extends Replicable<?, ?>, ReplicableImpl extends ReplicableInterface> {
    private static final Logger logger = Logger.getLogger(AbstractServerReplicationTest.class.getName());
    
    protected static final int SERVLET_PORT = 9990;
    protected ReplicableImpl replica;
    protected ReplicableImpl master;
    protected ReplicationServiceTestImpl<ReplicableInterface> replicaReplicator;
    private ReplicaDescriptor replicaDescriptor;
    private ReplicationServiceImpl masterReplicator;
    protected ReplicationMasterDescriptor  masterDescriptor;
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(5 * 60 * 1000); // timeout after 5 minutes
    private Thread initialLoadTestServerThread;

    /**
     * Sets up master and replica, starts the JMS message broker and registers the replica with the master. If you want
     * to drop the DB in your particular test case first, override {@link #persistenceSetUp(boolean)}. If you don't want
     * replication to start right away for your test, override this method, execute only {@link #basicSetUp(boolean, Replicable, Replicable)},
     * do what you need to do and then explicitly call {@link ReplicationServiceTestImpl#startToReplicateFrom(ReplicationMasterDescriptor)}
     * or {@link ReplicationServiceTestImpl#startToReplicateFromButDontYetFetchInitialLoad(ReplicationMasterDescriptor, boolean)}
     * on the first component returned by {@link #basicSetUp(boolean, Replicable, Replicable)}.
     */
    @Before
    public void setUp() throws Exception {
        try {
            Pair<ReplicationServiceTestImpl<ReplicableInterface>, ReplicationMasterDescriptor> result = basicSetUp(
                    /* dropDB */true, /* master=null means create a new one */null,
                    /* replica=null means create a new one */null);
            result.getA().startToReplicateFrom(result.getB());
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    /**
     * Gives subclasses a hook to set up a persistence component, such as a DB. This implementation does nothing.
     * 
     * @param dropDB if <code>true</code> the contents of the persistence layers are to be cleared
     */
    protected void persistenceSetUp(boolean dropDB) {
    }

    /**
     * Calls {@link #persistenceSetUp(boolean)} first. 
     * @param master
     *            if not <code>null</code>, the value will be used for {@link #master}; otherwise, a new racing event
     *            service will be created as master
     * @param replica
     *            if not <code>null</code>, the value will be used for {@link #replica}; otherwise, a new racing event
     *            service will be created as replica
     */
    protected Pair<ReplicationServiceTestImpl<ReplicableInterface>, ReplicationMasterDescriptor> basicSetUp(boolean dropDB,
            ReplicableImpl master, ReplicableImpl replica) throws Exception {
        persistenceSetUp(dropDB);
        String exchangeName = "test-sapsailinganalytics-exchange";
        String exchangeHost = "localhost";
        if (System.getenv(Activator.ENV_VAR_NAME_REPLICATION_HOST) != null) {
            exchangeHost = System.getenv(Activator.ENV_VAR_NAME_REPLICATION_HOST);
        }
        final UUID serverUuid = UUID.randomUUID();
        if (master != null) {
            this.master = master;
        } else {
            this.master = createNewMaster();
        }
        if (replica != null) {
            this.replica = replica;
        } else {
            this.replica = createNewReplica();
        }
        ReplicationInstancesManager rim = new ReplicationInstancesManager();
        masterReplicator = new ReplicationServiceImpl(exchangeName, exchangeHost, 0, rim, new SingletonReplicablesProvider(this.master));
        replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost(), serverUuid, "");
        masterReplicator.registerReplica(replicaDescriptor);
        // connect to exchange host and local server running as master
        // master server and exchange host can be two different hosts
        masterDescriptor = new ReplicationMasterDescriptorImpl(exchangeHost, exchangeName, 0, UUID.randomUUID().toString(), "localhost", SERVLET_PORT);
        ReplicationServiceTestImpl<ReplicableInterface> replicaReplicator = new ReplicationServiceTestImpl<ReplicableInterface>(exchangeName, exchangeHost, rim, replicaDescriptor,
                this.replica, this.master, masterReplicator, masterDescriptor);
        Pair<ReplicationServiceTestImpl<ReplicableInterface>, ReplicationMasterDescriptor> result = new Pair<>(replicaReplicator, masterDescriptor);
        initialLoadTestServerThread = replicaReplicator.startInitialLoadTransmissionServlet();
        this.replicaReplicator = replicaReplicator; 
        return result;
    }

    /**
     * Creates a new master replicable. This method is called only after {@link #persistenceSetUp(boolean)} has been called. Therefore,
     * any initialization that {@link #persistenceSetUp(boolean)} may have performed can be assumed to have taken place in the implementations
     * of this method.
     */
    abstract protected ReplicableImpl createNewMaster() throws Exception;

    /**
     * Creates a new replica replicable instance. This method is called only after {@link #persistenceSetUp(boolean)} has been called. Therefore,
     * any initialization that {@link #persistenceSetUp(boolean)} may have performed can be assumed to have taken place in the implementations
     * of this method.
     */
    abstract protected ReplicableImpl createNewReplica();

    @After
    public void tearDown() throws Exception {
        logger.info("starting to tearDown() test");
        persistenceTearDown();
        if (masterReplicator != null) {
            logger.info("before unregisterReplica...");
            masterReplicator.unregisterReplica(replicaDescriptor);
        }
        if (masterDescriptor != null) {
            logger.info("before stopConnection...");
            masterDescriptor.stopConnection();
        }
        try {
            if (initialLoadTestServerThread != null) {
                logger.info("found non-null initialLoadTestServerThread that we'll now try to stop...");
                URLConnection urlConnection = new URL("http://localhost:"+SERVLET_PORT+"/STOP").openConnection(); // stop the initial load test server thread
                urlConnection.getInputStream().close();
                logger.info("sent and closed STOP request");
                initialLoadTestServerThread.join(10000 /* wait 10s */);
                logger.info("joined servlet thread");
                assertFalse("Expected initial load test server thread to die", initialLoadTestServerThread.isAlive());
            }
        } catch (ConnectException ex) {
            /* do not make tests fail because of a server that has been shut down
             * or when an exception occured (see setUp()) - let the
             * original exception propagate */
            logger.log(Level.SEVERE, "Exception trying to connect to initial load test servlet to STOP it", ex);
        }
    }
    
    /**
     * Gives subclasses a hook to tear down a persistence component, such as a DB. This implementation does nothing.
     */
    protected void persistenceTearDown() {
    }

    public void stopReplicatingToMaster() throws IOException {
        replicaReplicator.stopToReplicateFromMaster();
    }
    
    public static class ReplicationServiceTestImpl<ReplicableInterface extends Replicable<?, ?>> extends ReplicationServiceImpl {
        protected static final int INITIAL_LOAD_PACKAGE_SIZE = 1024*1024;
        private final ReplicableInterface master;
        private final ReplicaDescriptor replicaDescriptor;
        private final ReplicationService masterReplicationService;
        private final ReplicationMasterDescriptor masterDescriptor;
        
        public ReplicationServiceTestImpl(String exchangeName, String exchangeHost, ReplicationInstancesManager replicationInstancesManager,
                ReplicaDescriptor replicaDescriptor, ReplicableInterface replica,
                ReplicableInterface master, ReplicationService masterReplicationService, ReplicationMasterDescriptor masterDescriptor)
                throws IOException {
            super(exchangeName, exchangeHost, 0, replicationInstancesManager, new SingletonReplicablesProvider(replica));
            this.replicaDescriptor = replicaDescriptor;
            this.master = master;
            this.masterReplicationService = masterReplicationService;
            this.masterDescriptor = masterDescriptor;
        }
        
        private Thread startInitialLoadTransmissionServlet() throws InterruptedException {
            final boolean[] listening = new boolean[] { false };
            Thread initialLoadTestServerThread = new Thread("Replication initial load test server") {
                public void run() {
                    ServerSocket ss = null;
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
                            logger.info("received request "+request);
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
                                master.serializeForInitialReplication(gzipOutputStream);
                                gzipOutputStream.finish();
                                ros.close();
                            } else if (request.contains("STOP")) {
                                stop = true;
                                logger.info("received STOP request");
                            }
                            pw.close();
                            s.close();
                            logger.info("Request handled successfully.");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            if (ss != null) {
                                ss.close();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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

        public ReplicationReceiver startToReplicateFromButDontYetFetchInitialLoad(ReplicationMasterDescriptor master, boolean startReplicatorSuspended)
                throws IOException {
            masterReplicationService.registerReplica(replicaDescriptor);
            registerReplicaUuidForMaster(replicaDescriptor.getUuid().toString(), master);
            QueueingConsumer consumer = master.getConsumer();
            final ReplicationReceiver replicator = new ReplicationReceiver(master, getReplicablesProvider(), startReplicatorSuspended, consumer);
            new Thread(replicator).start();
            return replicator;
        }
        
        /**
         * Clones the {@link #master}'s state to the {@link #replica} using
         * {@link RacingEventServiceImpl#serializeForInitialReplication(ObjectOutputStream)} and
         * {@link RacingEventServiceImpl#initiallyFillFrom(ObjectInputStream)} through a piped input/output stream.
         * @throws InterruptedException 
         */
        public void initialLoad() throws IOException, ClassNotFoundException, InterruptedException {
            PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);
            new Thread("clone writer") {
                public void run() {
                    try {
                        master.serializeForInitialReplication(pos);
                        pos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }.start();
            for (Replicable<?, ?> replicable : getReplicablesProvider().getReplicables()) {
                replicable.clearReplicaState();
                replicable.initiallyFillFrom(pis);
            }
            pis.close();
        }
        
        public void waitUntilQueueIsEmpty() throws InterruptedException, IllegalAccessException {
            synchronized (getReplicator()) {
                while (!getReplicator().isQueueEmpty()) {
                    getReplicator().wait();
                }
            }
        }
    }
}
