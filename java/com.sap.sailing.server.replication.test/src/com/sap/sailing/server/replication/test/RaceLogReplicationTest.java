package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboard;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;

public class RaceLogReplicationTest extends AbstractServerReplicationTest {
    private Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    private RaceLogEvent raceLogEvent;
    private RaceLogEvent anotherRaceLogEvent;
    
    @Before
    public void setUp() throws Exception {
        raceLogEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(new MillisecondsTimePoint(1), 42, RaceLogRaceStatus.UNKNOWN);
        anotherRaceLogEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(new MillisecondsTimePoint(2), 42, RaceLogRaceStatus.UNKNOWN);
        try {
            replicationDescriptorPair = basicSetUp(true, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }
    
    @Test
    public void testRaceLogEmptyOnInitialLoad() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta regatta = setupRegatta(regattaName);
        RaceLog masterLog = setupRaceColumn(regatta, seriesName, raceColumnName, fleetName);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, regatta);
        addAndValidate(masterLog, replicaLog);
    }
    
    @Test
    public void testRaceLogStateOnInitialLoad() throws InterruptedException, ClassNotFoundException, IOException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta masterRegatta = setupRegatta(regattaName);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        masterLog.add(raceLogEvent);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidate(masterLog, replicaLog);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta masterRegatta = setupRegatta(regattaName);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidate(masterLog, replicaLog, raceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidate(masterLog, replicaLog, raceLogEvent);
    }

    @Test
    public void testRaceEventReplicationOnRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        Regatta masterRegatta = setupRegatta(regattaName);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        masterLog.add(raceLogEvent);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidate(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationOnFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        masterLog.add(raceLogEvent);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidate(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    @Ignore
    public void testRaceEventReplicationOnRenamingFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        
        masterLog.add(raceLogEvent);
        
        RenameLeaderboard renameOperation = new RenameLeaderboard(leaderboardName, leaderboardName + "new");
        master.apply(renameOperation);
        Thread.sleep(3000);
        
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidate(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    private void addAndValidate(RaceLog masterLog, RaceLog replicaLog, RaceLogEvent... addedEvents) throws InterruptedException {
        // 1. Check state of replica after initial load...
        assertLogsEqual(masterLog, replicaLog);
        
        // 2. ... add all incoming events...
        for (RaceLogEvent event : addedEvents) {
            masterLog.add(event);
        }
        // 3. ... and give replication some time to deliver messages.
        Thread.sleep(3000);
        
        assertLogsEqual(masterLog, replicaLog);
    }

    private void assertLogsEqual(RaceLog masterLog, RaceLog replicaLog) {
        replicaLog.lockForRead();
        try {
            masterLog.lockForRead();
            try {
                assertEvents(masterLog.getRawFixes(), replicaLog.getRawFixes());
            } finally {
                masterLog.unlockAfterRead();
            }
        } finally {
            replicaLog.unlockAfterRead();
        }
    }

    private void assertEvents(Iterable<RaceLogEvent> expectedEvents, Iterable<RaceLogEvent> actualEvents) {
        Collection<RaceLogEvent> expectedCollection = new ArrayList<>();
        Util.addAll(expectedEvents, expectedCollection);
        
        Collection<RaceLogEvent> actualCollection = new ArrayList<>();
        Util.addAll(actualEvents, actualCollection);
        
        //assertEquals(expectedCollection.size(), actualCollection.size());
        assertEquals(Util.size(expectedEvents), Util.size(actualEvents));
        for (RaceLogEvent event : expectedEvents) {
            assertTrue(actualCollection.contains(event));
        }
    }

    private RaceLog getReplicaLog(final String fleetName, final String raceColumnName, Leaderboard leaderboard) {
        Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboard.getName());
        RaceColumn replicaColumn = replicaLeaderboard.getRaceColumnByName(raceColumnName);
        Fleet replicaFleet = replicaColumn.getFleetByName(fleetName);
        return replicaColumn.getRaceLog(replicaFleet);
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
    
    private RaceLog setupRaceColumn(final Regatta regatta, final String seriesName, 
            final String raceColumnName, final String fleetName) {
        AddColumnToSeries addColumnOperation = new AddColumnToSeries(regatta.getRegattaIdentifier(), seriesName, raceColumnName);
        RaceColumn raceColumn = master.apply(addColumnOperation);
        Fleet masterFleet = raceColumn.getFleetByName(fleetName);
        return raceColumn.getRaceLog(masterFleet);
    }

    private FlexibleLeaderboard setupFlexibleLeaderboard(final String leaderboardName) {
        CreateFlexibleLeaderboard createTestLeaderboard = new CreateFlexibleLeaderboard(leaderboardName, new int[] { 19, 44 }, new LowPoint(), null);
        FlexibleLeaderboard masterLeaderboard = master.apply(createTestLeaderboard);
        return masterLeaderboard;
    }
    
    private RaceLog setupRaceColumn(final String leaderboardName, final String fleetName, final String raceColumnName) {
        AddColumnToLeaderboard addColumnOperation = new AddColumnToLeaderboard(raceColumnName, leaderboardName, false);
        RaceColumn masterRaceColumn = master.apply(addColumnOperation);
        Fleet masterFleet = masterRaceColumn.getFleetByName(fleetName);
        RaceLog masterLog = masterRaceColumn.getRaceLog(masterFleet);
        return masterLog;
    }
    
    
}
