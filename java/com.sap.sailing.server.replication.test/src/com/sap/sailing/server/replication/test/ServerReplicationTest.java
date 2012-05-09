package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.DefaultLeaderboardName;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.EventName;
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
import com.sap.sailing.server.operationaltransformation.AddEvent;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;

public class ServerReplicationTest extends AbstractServerReplicationTest {
    @Test
    public void testBasicInitialLoad() throws Exception {
        assertNotSame(master, replica);
        assertEquals(Util.size(master.getAllEvents()), Util.size(replica.getAllEvents()));
        assertEquals(master.getLeaderboardGroups().size(), replica.getLeaderboardGroups().size());
        assertEquals(master.getLeaderboards().size(), replica.getLeaderboards().size());
        assertEquals(master.getLeaderboards().keySet(), replica.getLeaderboards().keySet());
    }
    
    @Test
    public void testLeaderboardCreationReplication() throws InterruptedException {
        Thread.sleep(1000); // wait 1s for JMS to deliver any recovered messages; there should be none
        final String leaderboardName = "My new leaderboard";
        assertNull(replica.getLeaderboardByName(leaderboardName));
        final int[] discardThresholds = new int[] { 17, 23 };
        CreateLeaderboard createTestLeaderboard = new CreateLeaderboard(leaderboardName, discardThresholds);
        assertNull(master.getLeaderboardByName(leaderboardName));
        master.apply(createTestLeaderboard);
        final Leaderboard masterLeaderboard = master.getLeaderboardByName(leaderboardName);
        assertNotNull(masterLeaderboard);
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        final Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNotNull(replicaLeaderboard);
        assertTrue(Arrays.equals(masterLeaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(),
                replicaLeaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces()));
    }

    @Test
    public void testLeaderboardRemovalReplication() throws InterruptedException {
        final String leaderboardName = DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME;
        assertNotNull(replica.getLeaderboardByName(leaderboardName));
        assertNotNull(master.getLeaderboardByName(leaderboardName));
        RemoveLeaderboard removeDefaultLeaderboard = new RemoveLeaderboard(leaderboardName);
        master.apply(removeDefaultLeaderboard);
        final Leaderboard masterLeaderboard = master.getLeaderboardByName(leaderboardName);
        assertNull(masterLeaderboard);
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        final Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNull(replicaLeaderboard);
    }
    
    @Test
    public void testWaypointRemovalReplication() throws InterruptedException {
        final String boatClassName = "49er";
        // FIXME use master DomainFactory; see bug 592
        final DomainFactory masterDomainFactory = DomainFactory.INSTANCE;
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName, /* typicallyStartsUpwind */ true);
        final String baseEventName = "Test Event";
        AddEvent addEventOperation = new AddEvent(baseEventName, boatClassName, /* boatClassTypicallyStartsUpwind */ true);
        Event event = master.apply(addEventOperation);
        final String raceName = "Test Race";
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        RaceDefinition race = new RaceDefinitionImpl(raceName, masterCourse, boatClass,
                new ArrayList<Competitor>());
        AddRaceDefinition addRaceOperation = new AddRaceDefinition(new EventName(event.getName()), race);
        master.apply(addRaceOperation);
        masterCourse.addWaypoint(0, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateBuoy("Buoy1")));
        masterCourse.addWaypoint(1, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateBuoy("Buoy2")));
        masterCourse.addWaypoint(2, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateBuoy("Buoy3")));
        masterCourse.removeWaypoint(1);
        Thread.sleep(1000); // wait 1s for JMS to deliver the message and the message to be applied
        Event replicaEvent = replica.getEvent(new EventName(event.getName()));
        assertNotNull(replicaEvent);
        RaceDefinition replicaRace = replicaEvent.getRaceByName(raceName);
        assertNotNull(replicaRace);
        Course replicaCourse = replicaRace.getCourse();
        assertEquals(2, Util.size(replicaCourse.getWaypoints()));
        assertEquals("Buoy1", replicaCourse.getFirstWaypoint().getBuoys().iterator().next().getName());
        assertEquals("Buoy3", replicaCourse.getLastWaypoint().getBuoys().iterator().next().getName());
    }
    
    @Test
    public void testStartTrackingRaceReplication() throws Exception {
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
        RacesHandle racesHandle = master.addRace(trackingParams, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */ 60000);
        while (!notifier[0]) {
            synchronized (notifier) {
                notifier.wait();
            }
        }
        EventAndRaceIdentifier raceIdentifier = racesHandle.getRaceTracker().getRaceIdentifiers().iterator().next();
        TrackedRace masterTrackedRace = master.getTrackedRace(raceIdentifier);
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
        racesHandle.getRaceTracker().stop();
    }
}
