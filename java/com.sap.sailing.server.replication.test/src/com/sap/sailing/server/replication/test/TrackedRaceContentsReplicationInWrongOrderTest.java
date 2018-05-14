package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFix;
import com.sap.sailing.server.operationaltransformation.RecordMarkGPSFixForNewMarkTrack;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateStartOfTracking;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Bug 4527 identified an issue, also related to bug 4530 and its fix: due to the way the {@link RaceListener}s are
 * registered, creating and sending the {@link CreateTrackedRace} operation and the tracking data-related
 * operations such as {@link RecordMarkGPSFix} happens in random order. When a replica chooses to have the
 * tracking data-related operations block for the appearance of the race then they may block endlessly under
 * certain unlikely but possible circumstances, such as when a tracked race is removed and the asynchronous
 * tracking data-related operations have been queued but the race has already been removed on the replica. This
 * endless blocking led to clogging the executor's threads used for asynchronous execution of tracking-related
 * operations.<p>
 * 
 * However, when not blocking but only checking for an existing tracked race (this was the fix introduced by
 * bug 4530) then these operations will be discarded and not applied if the tracked race-creating
 * {@link CreateTrackedRace} operations comes late. This will leave the replica with incomplete tracking
 * data; e.g., mark passings may be missing.<p>
 * 
 * A solution proposed by bug 4527, comment 20, would try to look for an existing tracked race in the
 * asynchronous operations and if not found register them for some time as callback listeners for the
 * race to be added. After a while they may be discarded, assuming that this could have been the "race removed"
 * case, so as to not leak memory with these operations. When the {@link CreateTrackedRace} operation then
 * comes late, the creation of the tracked race would unblock those waiting operations which then find their
 * tracked race and can then be applied.<p>
 * 
 * This test produces an "out of order" sequence of operations, first sending tracking-related operations,
 * then the {@link CreateTrackedRace} operation, and verifies that all tracking-related operations were
 * applied successfully.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedRaceContentsReplicationInWrongOrderTest extends AbstractServerReplicationTest {
    private Competitor competitor;
    private DynamicTrackedRace trackedRace;
    private RegattaNameAndRaceName raceIdentifier;
    private DynamicTrackedRegatta trackedRegatta;
    private Mark mark1, mark2, mark3;
    
    @Before
    public void setUp() throws Exception, UnknownHostException, InterruptedException {
        super.setUp();
        final String boatClassName = "49er";
        final DomainFactory masterDomainFactory = testSetUp.getMaster().getBaseDomainFactory();
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName, /* typicallyStartsUpwind */true);
        competitor = masterDomainFactory.getCompetitorAndBoatStore().getOrCreateCompetitor("GER 61", "Tina Lutz", "TL", Color.RED, "someone@nowhere.de", null, new TeamImpl("Tina Lutz + Susann Beucke",
                (List<PersonImpl>) Arrays.asList(new PersonImpl[] { new PersonImpl("Tina Lutz", masterDomainFactory.getOrCreateNationality("GER"), null, null),
                new PersonImpl("Tina Lutz", masterDomainFactory.getOrCreateNationality("GER"), null, null) }),
                new PersonImpl("Rigo de Mas", masterDomainFactory.getOrCreateNationality("NED"), null, null)),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
        Boat boat = masterDomainFactory.getCompetitorAndBoatStore().getOrCreateBoat("boat123", "boat123",
                masterDomainFactory.getOrCreateBoatClass("470", /* typicallyStartsUpwind */ true), "GER 61", null);
        Map<Competitor, Boat> competitorAndBoats = new HashMap<>();
        competitorAndBoats.put(competitor, boat);
        final String baseEventName = "Test Event";
        AddDefaultRegatta addEventOperation = new AddDefaultRegatta(RegattaImpl.getDefaultName(baseEventName, boatClassName), boatClassName, 
                /*startDate*/ null, /*endDate*/ null, UUID.randomUUID());
        Regatta regatta = master.apply(addEventOperation);
        final String raceName = "Test Race";
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        RaceDefinition race = new RaceDefinitionImpl(raceName, masterCourse, boatClass, competitorAndBoats);
        AddRaceDefinition addRaceOperation = new AddRaceDefinition(new RegattaName(regatta.getName()), race);
        master.apply(addRaceOperation);
        mark1 = masterDomainFactory.getOrCreateMark("Mark1");
        masterCourse.addWaypoint(0, masterDomainFactory.createWaypoint(mark1, /*passingInstruction*/ null));
        mark2 = masterDomainFactory.getOrCreateMark("Mark2");
        masterCourse.addWaypoint(1, masterDomainFactory.createWaypoint(mark2, /*passingInstruction*/ null));
        mark3 = masterDomainFactory.getOrCreateMark("Mark3");
        masterCourse.addWaypoint(2, masterDomainFactory.createWaypoint(mark3, /*passingInstruction*/ null));
        masterCourse.removeWaypoint(1);
        raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), raceName);
        trackedRegatta = master.apply(new TrackRegatta(raceIdentifier));
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
    public void testWrongOrder() throws UnknownHostException, MongoException, InterruptedException {
        final RecordMarkGPSFix markFixOperation = new RecordMarkGPSFixForNewMarkTrack(raceIdentifier, mark1, new GPSFixImpl(new DegreePosition(1, 2), MillisecondsTimePoint.now()));
        master.replicate(markFixOperation);
        trackedRace = (DynamicTrackedRace) master.apply(new CreateTrackedRace(raceIdentifier,
                MongoWindStoreFactory.INSTANCE.getMongoWindStore(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                        PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory()), /* delayToLiveInMillis */ 5000,
                /* millisecondsOverWhichToAverageWind */ 10000, /* millisecondsOverWhichToAverageSpeed */10000));
        master.apply(new UpdateStartOfTracking(raceIdentifier, new MillisecondsTimePoint(0)));
        trackedRace.waitUntilLoadingFromWindStoreComplete();
        Thread.sleep(1000);
        final TrackedRace replicaTrackedRace = replica.getTrackedRace(raceIdentifier);
        final Mark replicaMark1 = getMarkByName(replicaTrackedRace, mark1.getName());
        final GPSFixTrack<Mark, GPSFix> replicaMark1Track = replicaTrackedRace.getOrCreateTrack(replicaMark1);
        replicaMark1Track.lockForRead();
        try {
            assertEquals(1, Util.size(replicaMark1Track.getRawFixes()));
        } finally {
            replicaMark1Track.unlockAfterRead();
        }
    }
    
    private Mark getMarkByName(TrackedRace trackedRace, String markName) {
        for (final Mark mark : trackedRace.getMarks()) {
            if (mark.getName().equals(markName)) {
                return mark;
            }
        }
        return null;
    }
}
