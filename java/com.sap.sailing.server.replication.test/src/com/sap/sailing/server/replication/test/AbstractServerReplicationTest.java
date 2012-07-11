package com.sap.sailing.server.replication.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;

import com.rabbitmq.client.QueueingConsumer;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;
import com.sap.sailing.server.replication.impl.ReplicationMasterDescriptorImpl;
import com.sap.sailing.server.replication.impl.ReplicationServiceImpl;
import com.sap.sailing.server.replication.impl.Replicator;

public abstract class AbstractServerReplicationTest {
    private DomainFactory resolveAgainst;
    protected RacingEventServiceImpl replica;
    protected RacingEventServiceImpl master;
    private ReplicaDescriptor replicaDescriptor;
    private ReplicationServiceImpl masterReplicator;
    
    /**
     * Drops the test DB. Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        try {
            Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> result = basicSetUp(
                    true, /* master=null means create a new one */ null,
            /* replica=null means create a new one */null);
            result.getA().startToReplicateFrom(result.getB());
        } catch (Throwable t) {
            tearDown();
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
    protected Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> basicSetUp(
            boolean dropDB, RacingEventServiceImpl master, RacingEventServiceImpl replica) throws IOException {
        final String exchangeName = "test-sapsailinganalytics-exchange";
        final MongoDBService mongoDBService = MongoDBService.INSTANCE;
        if (dropDB) {
            mongoDBService.getDB().dropDatabase();
        }
        resolveAgainst = DomainFactory.INSTANCE;
        if (master != null) {
            this.master = master;
        } else {
            this.master = new RacingEventServiceImpl(mongoDBService);
        }
        if (replica != null) {
            this.replica = replica;
        } else {
            this.replica = new RacingEventServiceImpl(mongoDBService);
        }
        ReplicationInstancesManager rim = new ReplicationInstancesManager();
        masterReplicator = new ReplicationServiceImpl(exchangeName, rim, this.master);
        replicaDescriptor = new ReplicaDescriptor(InetAddress.getLocalHost());
        masterReplicator.registerReplica(replicaDescriptor);
        ReplicationMasterDescriptor masterDescriptor = new ReplicationMasterDescriptorImpl(null, exchangeName, 0, 0);
        ReplicationServiceTestImpl replicaReplicator = new ReplicationServiceTestImpl(exchangeName, resolveAgainst, rim,
                replicaDescriptor, this.replica, this.master, masterReplicator);
        Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> result = new Pair<>(replicaReplicator, masterDescriptor);
        return result;
    }

    @After
    public void tearDown() throws Exception {
        masterReplicator.unregisterReplica(replicaDescriptor);
    }

    static class ReplicationServiceTestImpl extends ReplicationServiceImpl {
        private final DomainFactory resolveAgainst;
        private final RacingEventService master;
        private final ReplicaDescriptor replicaDescriptor;
        private final ReplicationService masterReplicationService;
        
        public ReplicationServiceTestImpl(String exchangeName, DomainFactory resolveAgainst,
                ReplicationInstancesManager replicationInstancesManager, ReplicaDescriptor replicaDescriptor,
                RacingEventService replica, RacingEventService master, ReplicationService masterReplicationService)
                throws IOException {
            super(exchangeName, replicationInstancesManager, replica);
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
                ClassNotFoundException {
            Replicator replicator = startToReplicateFromButDontYetFetchInitialLoad(master, /* startReplicatorSuspended */ true);
            initialLoad();
            replicator.setSuspended(false); // resume after initial load
        }

        protected Replicator startToReplicateFromButDontYetFetchInitialLoad(ReplicationMasterDescriptor master, boolean startReplicatorSuspended)
                throws IOException {
            masterReplicationService.registerReplica(replicaDescriptor);
            registerReplicaUuidForMaster(replicaDescriptor.getUuid().toString(), master);
            QueueingConsumer consumer = master.getConsumer();
            final Replicator replicator = new Replicator(master, this, startReplicatorSuspended, consumer);
            new Thread(replicator).start();
            return replicator;
        }

        /**
         * Clones the {@link #master}'s state to the {@link #replica} using
         * {@link RacingEventServiceImpl#serializeForInitialReplication(ObjectOutputStream)} and
         * {@link RacingEventServiceImpl#initiallyFillFrom(ObjectInputStream)} through a piped input/output stream.
         */
        protected void initialLoad() throws IOException, ClassNotFoundException {
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
