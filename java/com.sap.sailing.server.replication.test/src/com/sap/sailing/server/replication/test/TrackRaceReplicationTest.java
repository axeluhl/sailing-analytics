package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.test.AbstractTracTracLiveTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;

public class TrackRaceReplicationTest extends AbstractServerReplicationTest {
    private static final Logger logger = Logger.getLogger(TrackRaceReplicationTest.class.getName());
    private TrackedRace masterTrackedRace;
    private RegattaAndRaceIdentifier raceIdentifier;
    private RaceHandle racesHandle;
    private final boolean[] notifier = new boolean[1];
    private RaceTrackingConnectivityParameters trackingParams;

    @Test
    public void testTearDownIsNotBeingCalledWhenSetUpFailsWithAnException() {
        // no-op
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final String eventID = "event_20110505_SailingTea";
        final String raceID = "bd8c778e-7c65-11e0-8236-406186cbf87c";
        URL paramURL = AbstractTracTracLiveTest.getParamURL(eventID, raceID);
        URI liveURI = AbstractTracTracLiveTest.getLiveURI();
        URI storedURI = AbstractTracTracLiveTest.getStoredURI();
        URI courseDesignUpdateURI = AbstractTracTracLiveTest.getCourseDesignUpdateURI();
        String tracTracUsername = AbstractTracTracLiveTest.getTracTracUsername();
        String tracTracPassword = AbstractTracTracLiveTest.getTracTracPassword();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2011, 05, 23, 13, 14, 31);
        MillisecondsTimePoint startOfTracking = new MillisecondsTimePoint(cal.getTimeInMillis());
        cal.set(2011, 05, 23, 15, 14, 31);
        MillisecondsTimePoint endOfTracking = new MillisecondsTimePoint(cal.getTimeInMillis());
        master.addOperationExecutionListener(new OperationExecutionListener<RacingEventService>() {
            @Override
            public <T> void executed(OperationWithResult<RacingEventService, T> operation) {
                if (operation instanceof CreateTrackedRace) {
                    synchronized (notifier) {
                        notifier[0] = true;
                        notifier.notifyAll();
                    }
                }
            }
        });
        trackingParams = com.sap.sailing.domain.tractracadapter.DomainFactory.INSTANCE
                .createTrackingConnectivityParameters(paramURL, liveURI, storedURI, courseDesignUpdateURI,
                        startOfTracking, endOfTracking, /* delayToLiveInMillis */
                        0l, /* simulateWithStartTimeNow */false, /*ignoreTracTracMarkPassings*/ false, EmptyRaceLogStore.INSTANCE,
                        EmptyRegattaLogStore.INSTANCE, tracTracUsername, tracTracPassword, "", "");
    }

    private void startTracking() throws Exception, InterruptedException {
        startTrackingOnMaster();
        waitForTrackRaceReplicationTrigger();
        raceIdentifier = racesHandle.getRaceTracker().getRaceIdentifiers().iterator().next();
        masterTrackedRace = master.getTrackedRace(raceIdentifier);
    }

    private void startTrackingOnMaster() throws Exception {
        racesHandle = master.addRace(/* regattaToAddTo */ null, trackingParams, /* timeoutInMilliseconds */ 60000);
    }

    private void waitForTrackRaceReplicationTrigger() throws InterruptedException, IllegalAccessException {
        while (!notifier[0]) {
            synchronized (notifier) {
                notifier.wait();
            }
        }
        replicaReplicator.waitUntilQueueIsEmpty();
    }
    
    @Test
    public void testStartTrackingRaceReplication() throws Exception {
        startTracking();
        Thread.sleep(1000);
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertNotNull(replicaTrackedRace);
        assertNotSame(masterTrackedRace, replicaTrackedRace);
        assertNotSame(masterTrackedRace.getRace(), replicaTrackedRace.getRace());
        assertEquals(Util.size(masterTrackedRace.getRace().getCompetitors()), Util.size(replicaTrackedRace.getRace().getCompetitors()));
        Leaderboard replicaDefaultLeaderboard = replica.getLeaderboardByName(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
        RaceColumn column = replicaDefaultLeaderboard.getRaceColumnByName(replicaTrackedRace.getRace().getName());
        assertNotNull(column);
        assertSame(replicaTrackedRace, column.getTrackedRace(replicaDefaultLeaderboard.getFleet(null)));
    }

    @Test
    public void testReassignmentToLeaderboardReplication() throws Exception {
        final String leaderboardName = "Test Leaderboard";
        Leaderboard masterLeaderboard = master.apply(new CreateFlexibleLeaderboard(leaderboardName, null, new int[0], new LowPoint(), null));
        final String columnName = "R1";
        RaceColumn masterColumn = master.apply(new AddColumnToLeaderboard(columnName, leaderboardName, /* medalRace */ false));
        final Fleet defaultFleet = masterLeaderboard.getFleet(null);
        // set the race identifier in the column; the tracked race
        // doesn't exist yet, but the race identifier is recorded already
        // anyway. When the tracked race then is loaded it is expected
        // to automatically be linked to the leaderboard column.
        master.apply(new ConnectTrackedRaceToLeaderboardColumn(leaderboardName, columnName, defaultFleet.getName(),
                new RegattaNameAndRaceName("Academy Tracking 2011 (STG)", "weym470may122011")));
        startTracking();
        assertNotNull(masterColumn.getTrackedRace(defaultFleet)); // ensure the re-assignment worked on the master
        Thread.sleep(1000);
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertNotNull(replicaTrackedRace);
        assertNotSame(masterTrackedRace, replicaTrackedRace);
        assertNotSame(masterTrackedRace.getRace(), replicaTrackedRace.getRace());
        assertEquals(Util.size(masterTrackedRace.getRace().getCompetitors()), Util.size(replicaTrackedRace.getRace().getCompetitors()));
        Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNotNull(replicaLeaderboard);
        RaceColumn column = replicaLeaderboard.getRaceColumnByName(columnName);
        assertNotNull(column);
        assertSame(replicaTrackedRace, column.getTrackedRace(defaultFleet));
    }
    
    @Test
    public void testRaceTimeReplication() throws InterruptedException, Exception {
        startTracking();
        // now wait at least until the start of tracking time has been received on the master copy
        boolean receivedStartAndEndOfTracking = master.getTrackedRace(raceIdentifier).getStartOfTracking() != null &&
                master.getTrackedRace(raceIdentifier).getEndOfTracking() != null;
        while (!receivedStartAndEndOfTracking) { // relying on the Timeout rule for this test
            Thread.sleep(10);
            receivedStartAndEndOfTracking = master.getTrackedRace(raceIdentifier).getStartOfTracking() != null &&
                    master.getTrackedRace(raceIdentifier).getEndOfTracking() != null;
        }
        Thread.sleep(1000);
        logger.info("verifying replica's state");
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertEquals(masterTrackedRace.getStartOfTracking(), replicaTrackedRace.getStartOfTracking());
        assertEquals(masterTrackedRace.getEndOfTracking(), replicaTrackedRace.getEndOfTracking());
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        assertFalse(now.equals(replicaTrackedRace.getStartOfRace()));
        ((DynamicTrackedRace) masterTrackedRace).setStartTimeReceived(now);
        Thread.sleep(1000);
        assertEquals(now, replicaTrackedRace.getStartOfRace());
    }

    @Test
    public void testWindSourcesToExcludeReplication() throws InterruptedException, Exception {
        startTracking();
        Thread.sleep(1000);
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertTrue(Util.isEmpty(masterTrackedRace.getWindSourcesToExclude()));
        assertTrue(Util.isEmpty(replicaTrackedRace.getWindSourcesToExclude()));
        masterTrackedRace.setWindSourcesToExclude(Collections.singleton(new WindSourceImpl(WindSourceType.WEB)));
        assertEquals(1, Util.size(masterTrackedRace.getWindSourcesToExclude()));
        assertEquals(WindSourceType.WEB, masterTrackedRace.getWindSourcesToExclude().iterator().next().getType());
        Thread.sleep(1000);
        assertEquals(1, Util.size(replicaTrackedRace.getWindSourcesToExclude()));
        assertEquals(WindSourceType.WEB, replicaTrackedRace.getWindSourcesToExclude().iterator().next().getType());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        if (racesHandle != null) {
            racesHandle.getRaceTracker().stop(/* preemptive */ false);
        }
        super.tearDown();
    }
}
