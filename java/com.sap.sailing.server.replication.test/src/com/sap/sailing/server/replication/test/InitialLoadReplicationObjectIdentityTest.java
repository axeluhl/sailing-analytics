package com.sap.sailing.server.replication.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.test.TrackBasedTest;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.ReplicationReceiver;

public class InitialLoadReplicationObjectIdentityTest extends AbstractServerReplicationTest {
    private Pair<com.sap.sse.replication.testsupport.AbstractServerReplicationTest.ReplicationServiceTestImpl<RacingEventService>, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    /**
     * Drops the test DB. Sets up master and replica, starts the JMS message broker and registers the replica with the master.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        persistenceSetUp(/* dropDB */ true);
        this.master = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService, DomainFactory.INSTANCE), PersistenceFactory.INSTANCE
                .getMongoObjectFactory(mongoDBService), MediaDBFactory.INSTANCE.getMediaDB(mongoDBService), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        this.replica = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoDBService, DomainFactory.INSTANCE), PersistenceFactory.INSTANCE
                .getMongoObjectFactory(mongoDBService), MediaDBFactory.INSTANCE.getMediaDB(mongoDBService), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
    }
    
    private void performReplicationSetup() throws Exception {
        try {
            replicationDescriptorPair = basicSetUp(/* dropDB */ false, this.master, this.replica);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }
    
    @Test
    public void testInitialLoad() throws Exception {
        /* Event */
        String eventName = "Monster Event";
        String venue = "Default Venue";
        List<String> courseAreaNames = new ArrayList<String>();
        courseAreaNames.add("Default");
        final UUID eventId = UUID.randomUUID();
        final TimePoint eventStartDate = new MillisecondsTimePoint(new Date());
        final TimePoint eventEndDate = new MillisecondsTimePoint(new Date());

        Event event = master.addEvent(eventName, /* eventDescription */ null, eventStartDate, eventEndDate, venue, false, eventId);
        assertNotNull(master.getEvent(eventId));
        assertNull(replica.getEvent(eventId));
        
        /* Regatta */
        final String baseEventName = "Kiel Week 2012";
        final String boatClassName = "49er";
        final Iterable<Series> series = Collections.emptyList();
        Regatta masterRegatta = master.createRegatta(RegattaImpl.getDefaultName(baseEventName, boatClassName), boatClassName, UUID.randomUUID(), series,
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /* useStartTimeInference */ true);
        assertNotNull(master.getRegatta(masterRegatta.getRegattaIdentifier()));
        assertTrue(master.getAllRegattas().iterator().hasNext());
        assertNull(replica.getRegatta(masterRegatta.getRegattaIdentifier()));
        
        /* Leaderboard */
        final String leaderboardName = "Great Leaderboard";
        final int[] discardThresholds = new int[] { 17, 23 };
        CreateFlexibleLeaderboard createTestLeaderboard = new CreateFlexibleLeaderboard(leaderboardName, null, discardThresholds, new LowPoint(), null);
        assertNull(master.getLeaderboardByName(leaderboardName));
        master.apply(createTestLeaderboard);
        assertNotNull(master.getLeaderboardByName(leaderboardName));
        assertNull(replica.getLeaderboardByName(leaderboardName));

        /* LeaderboardGroup */
        final String leaderBoardGroupName = "Great Leaderboard Group";
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboardName);
        int[] overallLeaderboardDiscardThresholds = new int[] {};
        ScoringSchemeType overallLeaderboardScoringSchemeType = ScoringSchemeType.HIGH_POINT;
        LeaderboardGroup leaderboardGroup = master.addLeaderboardGroup(UUID.randomUUID(), leaderBoardGroupName, "Some descriptive Description",
                "displayName", false, leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        assertNotNull(master.getLeaderboardGroupByName(leaderBoardGroupName));
        assertNull(replica.getLeaderboardGroupByName(leaderBoardGroupName));
        
        event.addLeaderboardGroup(leaderboardGroup);
        
        /* Media Library */
        Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        assignedRaces.add(new RegattaNameAndRaceName("49er", "R1"));
        MediaTrack mediaTrack1 = new MediaTrack("title-1", "url", MillisecondsTimePoint.now(), MillisecondsDurationImpl.ONE_HOUR, MimeType.mp4, assignedRaces);
        master.mediaTrackAdded(mediaTrack1);
        MediaTrack mediaTrack2 = new MediaTrack("title-2", "url", MillisecondsTimePoint.now(), MillisecondsDurationImpl.ONE_HOUR, MimeType.ogv, assignedRaces);
        master.mediaTrackAdded(mediaTrack2);
        MediaTrack mediaTrack3 = new MediaTrack("title-3", "url", MillisecondsTimePoint.now(), MillisecondsDurationImpl.ONE_HOUR, MimeType.mp4, assignedRaces);
        master.mediaTrackAdded(mediaTrack3);
        
        /* fire up replication */
        performReplicationSetup();
        ReplicationMasterDescriptor the_master = replicationDescriptorPair.getB(); /* master descriptor */
        ReplicationReceiver replicator = replicationDescriptorPair.getA().startToReplicateFromButDontYetFetchInitialLoad(the_master, /* startReplicatorSuspended */ true);
        replicationDescriptorPair.getA().initialLoad();
        replicator.setSuspended(false);
        synchronized (replicator) {
            while (!replicator.isQueueEmpty()) {
                replicator.wait();
            }
        }

        Event replicaEvent = replica.getEvent(eventId);
        assertNotNull(replicaEvent);
        assertNotNull(replica.getRegatta(masterRegatta.getRegattaIdentifier()));
        LeaderboardGroup replicaLeaderboardGroup = replica.getLeaderboardGroupByName(leaderBoardGroupName);
        assertNotNull(replicaLeaderboardGroup);
        assertNotNull(replica.getLeaderboardByName(leaderboardName));
        assertTrue(replica.getAllRegattas().iterator().hasNext());
        assertSame(replicaLeaderboardGroup, replicaEvent.getLeaderboardGroups().iterator().next());
        //System.out.println("InitialLoadReplicationObjectIdentityTest.testInitialLoad - replica.getAllMediaTracks: " + replica.getAllMediaTracks());
        
        assertThat(replica.getAllMediaTracks().size(), is(3));
    }

    @Test
    public void testSameCompetitorInTwoRacesReplication() throws Exception {
        performReplicationSetup();
        final String boatClassName = "49er";
        final DomainFactory masterDomainFactory = master.getBaseDomainFactory();
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName);
        final String baseEventName = "Test Event";
        AddDefaultRegatta addEventOperation = new AddDefaultRegatta(RegattaImpl.getDefaultName(baseEventName, boatClassName), boatClassName, UUID.randomUUID());
        Regatta regatta = master.apply(addEventOperation);
        final String raceName1 = "Test Race 1";
        final String raceName2 = "Test Race 2";
        Competitor competitor = TrackBasedTest.createCompetitor("The Same Competitor");
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        final ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        competitors.add(competitor);
        RaceDefinition race1 = new RaceDefinitionImpl(raceName1, masterCourse, boatClass, competitors);
        AddRaceDefinition addRaceOperation1 = new AddRaceDefinition(new RegattaName(regatta.getName()), race1);
        master.apply(addRaceOperation1);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceDefinition race2 = new RaceDefinitionImpl(raceName2, masterCourse, boatClass, competitors);
        AddRaceDefinition addRaceOperation2 = new AddRaceDefinition(new RegattaName(regatta.getName()), race2);
        master.apply(addRaceOperation2);
        Thread.sleep(3000); // wait 1s for messaging to deliver the message and the message to be applied
        Regatta replicaEvent = replica.getRegatta(new RegattaName(regatta.getName()));
        RaceDefinition replicaRace1 = replicaEvent.getRaceByName(raceName1);
        RaceDefinition replicaRace2 = replicaEvent.getRaceByName(raceName2);
        assertSame(replicaRace1.getCompetitors().iterator().next(), replicaRace2.getCompetitors().iterator().next());
    }
}
