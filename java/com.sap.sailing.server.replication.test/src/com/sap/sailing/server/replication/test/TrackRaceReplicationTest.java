package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.net.URI;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.test.AbstractTracTracLiveTest;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.OperationExecutionListener;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;

public class TrackRaceReplicationTest extends AbstractServerReplicationTest {
    private TrackedRace masterTrackedRace;
    private EventAndRaceIdentifier raceIdentifier;
    private RacesHandle racesHandle;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final String eventID = "event_20110505_SailingTea";
        final String raceID = "bd8c778e-7c65-11e0-8236-406186cbf87c";
        URL paramURL = AbstractTracTracLiveTest.getParamURL(eventID, raceID);
        URI liveURI = AbstractTracTracLiveTest.getLiveURI();
        URI storedURI = AbstractTracTracLiveTest.getStoredURI();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2011, 05, 23, 13, 14, 31);
        MillisecondsTimePoint startOfTracking = new MillisecondsTimePoint(cal.getTimeInMillis());
        cal.set(2011, 05, 23, 15, 14, 31);
        MillisecondsTimePoint endOfTracking = new MillisecondsTimePoint(cal.getTimeInMillis());
        final boolean[] notifier = new boolean[1];
        master.addOperationExecutionListener(new OperationExecutionListener() {
            @Override
            public <T> void executed(RacingEventServiceOperation<T> operation) {
                if (operation instanceof CreateTrackedRace) {
                    synchronized (notifier) {
                        notifier[0] = true;
                        notifier.notifyAll();
                    }
                }
            }
        });
        RaceTrackingConnectivityParameters trackingParams = com.sap.sailing.domain.tractracadapter.DomainFactory.INSTANCE.createTrackingConnectivityParameters(paramURL,
                liveURI, storedURI, startOfTracking, endOfTracking, EmptyWindStore.INSTANCE);
        racesHandle = master.addRace(trackingParams, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        while (!notifier[0]) {
            synchronized (notifier) {
                notifier.wait();
            }
        }
        raceIdentifier = racesHandle.getRaceTracker().getRaceIdentifiers().iterator().next();
        masterTrackedRace = master.getTrackedRace(raceIdentifier);
    }
    
    @Test
    public void testStartTrackingRaceReplication() throws Exception {
        Thread.sleep(1000);
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertNotNull(replicaTrackedRace);
        assertNotSame(masterTrackedRace, replicaTrackedRace);
        assertNotSame(masterTrackedRace.getRace(), replicaTrackedRace.getRace());
        assertEquals(Util.size(masterTrackedRace.getRace().getCompetitors()), Util.size(replicaTrackedRace.getRace().getCompetitors()));
        Leaderboard replicaDefaultLeaderboard = replica.getLeaderboardByName(DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME);
        RaceInLeaderboard column = replicaDefaultLeaderboard.getRaceColumnByName(replicaTrackedRace.getRace().getName());
        assertNotNull(column);
        assertSame(replicaTrackedRace, column.getTrackedRace());
        tearDown();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        racesHandle.getRaceTracker().stop();
        super.tearDown();
    }
}
