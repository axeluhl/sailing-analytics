package com.sap.sailing.server.replication.test;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.ReplicationMasterDescriptor;

public class RegattaLogReplicationTest extends
        AbstractLogReplicationTest<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> {

    private Pair<com.sap.sse.replication.testsupport.AbstractServerReplicationTest.ReplicationServiceTestImpl<RacingEventService>, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    private RegattaLogEvent regattaLogEvent;
//    private RegattaLogEvent anotherRegattaLogEvent;
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);
    
    private TimePoint t(long millis) {
        return new MillisecondsTimePoint(millis);
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        regattaLogEvent = new RegattaLogRegisterCompetitorEventImpl(t(0), author, t(0), 0, null);
//        anotherRegattaLogEvent = new RegattaLogRegisterCompetitorEventImpl(t(1), author, t(1), 1, null);
        try {
            replicationDescriptorPair = basicSetUp(/* dropDB */ true, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }
    
    @Test
    public void testRegattaLogEmptyOnInitialLoad() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        Regatta regatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RegattaLog masterLog = regatta.getRegattaLog();
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RegattaLog replicaLog = getReplicaLog(regatta);
        addAndValidateEventIds(masterLog, replicaLog);
    }
    
    @Test
    public void testRegattaLogStateOnInitialLoad() throws InterruptedException, ClassNotFoundException, IOException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        Regatta regatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RegattaLog masterLog = regatta.getRegattaLog();
        masterLog.add(regattaLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RegattaLog replicaLog = getReplicaLog(regatta);
        addAndValidateEventIds(masterLog, replicaLog);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RegattaLog masterLog = masterRegatta.getRegattaLog();
        masterLog.add(regattaLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RegattaLog replicaLog = getReplicaLog(masterRegatta);
        addAndValidateEventIds(masterLog, replicaLog, regattaLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RegattaLog masterLog = masterLeaderboard.getRegattaLog();
        masterLog.add(regattaLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RegattaLog replicaLog = getReplicaLog(masterLeaderboard);
        addAndValidateEventIds(masterLog, replicaLog, regattaLogEvent);
    }


    private RegattaLog getReplicaLog(Leaderboard leaderboard) {
        return ((FlexibleLeaderboard) replica.getLeaderboardByName(leaderboard.getName())).getRegattaLog();
    }

    private RegattaLog getReplicaLog(Regatta masterRegatta) {
        return replica.getRegatta(masterRegatta.getRegattaIdentifier()).getRegattaLog();
    }
}
