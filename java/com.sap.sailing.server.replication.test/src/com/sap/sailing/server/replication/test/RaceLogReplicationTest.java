package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class RaceLogReplicationTest extends AbstractServerReplicationTest {
    private Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    private RaceLogEvent raceLogEvent;
    
    @Before
    public void setUp() throws Exception {
        raceLogEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(null, 42, RaceLogRaceStatus.UNKNOWN);
        try {
            replicationDescriptorPair = basicSetUp(true, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }
    
    @Test
    public void testRaceLogEmptyOnInitialLoad() throws ClassNotFoundException, IOException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta regatta = setupRegatta(regattaName);
        setupRaceColumn(regatta, seriesName, raceColumnName);
        
        // 2. ... and start to replicate!
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, regatta);
        replicaLog.lockForRead();
        try {
            assertNotNull(replicaLog);
            assertTrue(Util.isEmpty(replicaLog.getRawFixes()));
        } finally {
            replicaLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testRaceLogStateOnInitialLoad() throws ClassNotFoundException, IOException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta masterRegatta = setupRegatta(regattaName);
        RaceColumn masterRaceColumn = setupRaceColumn(masterRegatta, seriesName, raceColumnName);
        Fleet masterFleet = masterRaceColumn.getFleetByName(fleetName);
        
        RaceLog masterLog = masterRaceColumn.getRaceLog(masterFleet);
        masterLog.add(raceLogEvent);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        replicaLog.lockForRead();
        try {
            assertFalse(Util.isEmpty(replicaLog.getRawFixes()));
            assertEquals(1, Util.size(replicaLog.getRawFixes()));
            assertEquals(raceLogEvent.getId(), Util.get(replicaLog.getRawFixes(), 0).getId());
        } finally {
            replicaLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testRaceEventReplication() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta masterRegatta = setupRegatta(regattaName);
        RaceColumn masterRaceColumn = setupRaceColumn(masterRegatta, seriesName, raceColumnName);
        Fleet masterFleet = masterRaceColumn.getFleetByName(fleetName);
        
        RaceLog masterLog = masterRaceColumn.getRaceLog(masterFleet);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        
        // We expect an empty race log after initial load.
        replicaLog.lockForRead();
        try {
            assertTrue(Util.isEmpty(replicaLog.getRawFixes()));
        } finally {
            replicaLog.unlockAfterRead();
        }
        
        masterLog.add(raceLogEvent);
        Thread.sleep(3000);     // give replication some time to deliver messages...
        
        
        // Now the event should be replicated.
        replicaLog.lockForRead();
        try {
            assertFalse(Util.isEmpty(replicaLog.getRawFixes()));
            assertEquals(1, Util.size(replicaLog.getRawFixes()));
            assertEquals(raceLogEvent.getId(), Util.get(replicaLog.getRawFixes(), 0).getId());
        } finally {
            replicaLog.unlockAfterRead();
        }
    }

    private RaceLog getReplicaLog(final String seriesName, final String fleetName, final String raceColumnName,
            Regatta masterRegatta) {
        Regatta replicaRegatta = replica.getRegatta(masterRegatta.getRegattaIdentifier());
        RaceColumn replicaColumn = replicaRegatta.getSeriesByName(seriesName).getRaceColumnByName(raceColumnName);
        Fleet replicaFleet = replicaColumn.getFleetByName(fleetName);
        
        return replicaColumn.getRaceLog(replicaFleet);
    }

    private Regatta setupRegatta(final String regattaName) {
        // 1. Install some race column on master...
        AddDefaultRegatta addRegattaOperation = new AddDefaultRegatta(regattaName, "49er", UUID.randomUUID());
        return master.apply(addRegattaOperation);
    }
    
    private RaceColumn setupRaceColumn(final Regatta regatta,  final String seriesName, final String raceColumnName) {
        AddColumnToSeries addColumnOperation = new AddColumnToSeries(regatta.getRegattaIdentifier(), seriesName, raceColumnName);
        return master.apply(addColumnOperation);
    }
}
