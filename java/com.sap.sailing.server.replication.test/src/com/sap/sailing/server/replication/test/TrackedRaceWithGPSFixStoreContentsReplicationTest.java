package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStore;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStoreFactory;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.testsupport.AbstractServerReplicationTestSetUp.ReplicationServiceTestImpl;

/**
 * Runs the same tests as {@link TrackedRaceContentsReplicationTest}, but with a non-empty {@link MongoGPSFixStore} that
 * has special serialization requirements.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedRaceWithGPSFixStoreContentsReplicationTest extends AbstractServerReplicationTest {
    private Competitor competitor;
    private DynamicTrackedRace trackedRace;
    private RegattaNameAndRaceName raceIdentifier;
    private DynamicTrackedRegatta trackedRegatta;
    
    @Before
    public void setUp() throws Exception, UnknownHostException, InterruptedException {
        final GPSFixStore gpsFixStore = MongoGPSFixStoreFactory.INSTANCE.getMongoGPSFixStore(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), /* serviceFinderFactory */ null);
        Pair<ReplicationServiceTestImpl<RacingEventService>, ReplicationMasterDescriptor> replicationDescriptors = super.basicSetUp(
                /* dropDB */true, /* master=null means create a new one */null,
                /* replica=null means create a new one */null);
        final String boatClassName = "49er";
        final DomainFactory masterDomainFactory = testSetUp.getMaster().getBaseDomainFactory();
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName, /* typicallyStartsUpwind */true);
        competitor = masterDomainFactory.getCompetitorStore().getOrCreateCompetitor("GER 61", "Tina Lutz", Color.RED, "someone@nowhere.de", null, new TeamImpl("Tina Lutz + Susann Beucke",
                (List<PersonImpl>) Arrays.asList(new PersonImpl[] { new PersonImpl("Tina Lutz", DomainFactory.INSTANCE.getOrCreateNationality("GER"), null, null),
                new PersonImpl("Tina Lutz", DomainFactory.INSTANCE.getOrCreateNationality("GER"), null, null) }),
                new PersonImpl("Rigo de Mas", DomainFactory.INSTANCE.getOrCreateNationality("NED"), null, null)),
                new BoatImpl("GER 61", DomainFactory.INSTANCE.getOrCreateBoatClass("470", /* typicallyStartsUpwind */ true), "GER 61"),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null);
        final String baseEventName = "Test Event";
        AddDefaultRegatta addEventOperation = new AddDefaultRegatta(RegattaImpl.getDefaultName(baseEventName, boatClassName), boatClassName, 
                /*startDate*/ null, /*endDate*/ null, UUID.randomUUID());
        Regatta regatta = master.apply(addEventOperation);
        final String raceName = "Test Race";
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        RaceDefinition race = new RaceDefinitionImpl(raceName, masterCourse, boatClass, Collections.singletonList(competitor));
        AddRaceDefinition addRaceOperation = new AddRaceDefinition(new RegattaName(regatta.getName()), race);
        master.apply(addRaceOperation);
        masterCourse.addWaypoint(0, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateMark("Mark1"), /*passingInstruction*/ null));
        masterCourse.addWaypoint(1, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateMark("Mark2"), /*passingInstruction*/ null));
        masterCourse.addWaypoint(2, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateMark("Mark3"), /*passingInstruction*/ null));
        masterCourse.removeWaypoint(1);
        raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), raceName);
        trackedRegatta = master.apply(new TrackRegatta(raceIdentifier));
        trackedRace = (DynamicTrackedRace) master.apply(new CreateTrackedRace(raceIdentifier,
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                        PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory()), gpsFixStore, /* delayToLiveInMillis */ 5000,
                /* millisecondsOverWhichToAverageWind */ 10000, /* millisecondsOverWhichToAverageSpeed */10000));
        trackedRace.waitUntilLoadingFromWindStoreComplete();
        // set up the tracked race on the master with a non-empty GPS fix store before starting replication; this shall
        // test serialization with a non-empty GPS fix store set on the tracked race. See also bug 2986.
        replicationDescriptors.getA().startToReplicateFrom(replicationDescriptors.getB());
    }
    
    protected Competitor getCompetitor() {
        return competitor;
    }

    protected DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    protected RegattaNameAndRaceName getRaceIdentifier() {
        return raceIdentifier;
    }

    protected DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    @Test
    public void testGPSFixReplication() throws InterruptedException {
        final GPSFixMovingImpl fix = new GPSFixMovingImpl(new DegreePosition(1, 2), new MillisecondsTimePoint(12345),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(123)));
        trackedRace.recordFix(competitor, fix);
        Thread.sleep(1000);
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        Competitor replicaCompetitor = replicaTrackedRace.getRace().getCompetitors().iterator().next();
        assertNotNull(replicaCompetitor);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = replicaTrackedRace.getTrack(replicaCompetitor);
        competitorTrack.lockForRead();
        try {
            assertEquals(1, Util.size(competitorTrack.getRawFixes()));
            assertEquals(fix, competitorTrack.getRawFixes().iterator().next());
            assertNotSame(fix, competitorTrack.getRawFixes().iterator().next());
        } finally {
            competitorTrack.unlockAfterRead();
        }
    }

    @Test
    public void testTrackedRaceHasValidRaceLogResolverAfterDeserialization() {
        TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        assertNotNull(replicaTrackedRace.getRaceLogResolver());
    }
}
