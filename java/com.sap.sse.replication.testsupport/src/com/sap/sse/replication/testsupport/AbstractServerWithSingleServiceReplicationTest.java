package com.sap.sse.replication.testsupport;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.testsupport.AbstractServerReplicationTestSetUp.ReplicationServiceTestImpl;

public abstract class AbstractServerWithSingleServiceReplicationTest<ReplicableInterface extends Replicable<?, ?>, ReplicableImpl extends ReplicableInterface> {
    protected final AbstractServerReplicationTestSetUp<ReplicableInterface, ReplicableImpl> testSetUp;
    protected ReplicableImpl replica;
    protected ReplicableImpl master;
    protected ReplicationServiceTestImpl<ReplicableInterface> replicaReplicator;
    protected ReplicationMasterDescriptor  masterDescriptor;
    
    
    public AbstractServerWithSingleServiceReplicationTest(AbstractServerReplicationTestSetUp<ReplicableInterface, ReplicableImpl> testSetUp) {
        this.testSetUp = testSetUp;
    }

    @Before
    public void setUp() throws Exception {
        testSetUp.setUp();
        master = testSetUp.getMaster();
        replica = testSetUp.getReplica();
        replicaReplicator = testSetUp.getReplicaReplicator();
        masterDescriptor = testSetUp.getMasterDescriptor();
    }
    
    protected Pair<ReplicationServiceTestImpl<ReplicableInterface>, ReplicationMasterDescriptor> basicSetUp(boolean dropDB,
            ReplicableImpl master, ReplicableImpl replica) throws Exception {
        return testSetUp.basicSetUp(dropDB, master, replica);
    }
    
    @After
    public void tearDown() throws Exception {
        testSetUp.tearDown();
    }
    
    protected ReplicableImpl createNewMaster() throws Exception {
        return testSetUp.createNewMaster();
    }
    
    protected ReplicableImpl createNewReplica() throws Exception {
        return testSetUp.createNewReplica();
    }
    
    protected void persistenceSetUp(boolean dropDB) {
        testSetUp.persistenceSetUp(dropDB);
    }
    
    protected void persistenceTearDown() {
        testSetUp.persistenceTearDown();
    }
    
    public void stopReplicatingToMaster() throws IOException {
        testSetUp.stopReplicatingToMaster();
    }
}
