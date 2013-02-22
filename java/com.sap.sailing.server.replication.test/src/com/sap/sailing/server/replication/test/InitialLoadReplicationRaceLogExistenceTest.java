package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class InitialLoadReplicationRaceLogExistenceTest extends AbstractServerReplicationTest {
    private Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    /**
     * Drops the test DB. Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    public void setUp() throws Exception {
        try {
            replicationDescriptorPair = basicSetUp(true, /* master=null means create a new one */ null,
            /* replica=null means create a new one */null);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }

    @Test
    public void testRaceLogInInitialLoadReplication() throws InterruptedException, ClassNotFoundException, IOException {
        final String boatClassName = "49er";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String baseRegattaName = "Test Regatta";
        final String raceName1 = "Test Race 1";
        final String raceName2 = "Test Race 2";
        
        AddDefaultRegatta addRegattaOperation = new AddDefaultRegatta(baseRegattaName, boatClassName, UUID.randomUUID());
        Regatta regatta = master.apply(addRegattaOperation);
        
        AddColumnToSeries addColumnOperation = new AddColumnToSeries(regatta.getRegattaIdentifier(), seriesName, raceName1);
        RaceColumn raceColumn = master.apply(addColumnOperation);
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        
        // TODO Create a race log event and add it to the log before the initial load starts
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        AddColumnToSeries addColumnOperation2 = new AddColumnToSeries(regatta.getRegattaIdentifier(), seriesName, raceName2);
        master.apply(addColumnOperation2);
        
        
        Thread.sleep(3000); // wait 3s for messaging to deliver the message and the message to be applied
        Regatta replicaRegatta = replica.getRegatta(new RegattaName(regatta.getName()));
        Series replicaSeries = replicaRegatta.getSeriesByName(seriesName);
        RaceColumn replicaColumn = replicaSeries.getRaceColumnByName(raceName1);
        Fleet replicaFleet = replicaColumn.getFleetByName(fleetName);
        
        // TODO Retrieve race log event from replica
        raceColumn.getRaceLog(fleet).lockForRead();
        try {

        	replicaColumn.getRaceLog(replicaFleet).lockForRead();
        	try {
        		assertEquals(Util.size(raceColumn.getRaceLog(fleet).getFixes()), Util.size(replicaColumn.getRaceLog(replicaFleet).getFixes()));
        	} finally {
        		replicaColumn.getRaceLog(replicaFleet).unlockAfterRead();
        	}
        } finally {
        	raceColumn.getRaceLog(fleet).unlockAfterRead();
        }

        // TODO test race log event for assertSame
    }
}
