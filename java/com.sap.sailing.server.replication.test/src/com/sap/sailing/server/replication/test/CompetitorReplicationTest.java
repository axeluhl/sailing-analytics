package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.AllowCompetitorResetToDefaults;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitor;

/**
 * Tests replication of competitors in conjunction with the {@link CompetitorStore} concepts, particularly the
 * possibility to allow for competitor data to be updated, either explicitly or implicitly from a tracking provider
 * after marking the competitor using
 * {@link CompetitorStore#allowCompetitorResetToDefaults(com.sap.sailing.domain.base.Competitor)}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorReplicationTest extends AbstractServerReplicationTest {
    /**
     * Add a tracked race to the master that includes a competitor; check that the competiotr was properly replicated to
     * the replica's {@link CompetitorStore}. Afterwards, use the {@link UpdateCompetitor} operation on the master to
     * perform an explicit update; ensure that the update arrived on the replica. Then execute an
     * {@link AllowCompetitorResetToDefaults} operation on the master, afterwards update the competitor on the master,
     * then force some competitor-related event to be sent to the replica, such as a GPS fix update. This will serialize
     * the competitor with its modified state over to the replica where a
     * {@link DomainFactory#getOrCreateCompetitor(java.io.Serializable, String, com.sap.sailing.domain.base.impl.DynamicTeam, com.sap.sailing.domain.base.impl.DynamicBoat)}
     * will be triggered. This should also update the competitor on the client.
     */
    @Test
    public void testSimpleSpecificRegattaReplication() throws InterruptedException {
        String baseEventName = "My Test Event";
        String boatClassName = "Kielzugvogel";
        Integer regattaId = 12345;
        Iterable<Series> series = Collections.emptyList();
        Regatta masterRegatta = master.createRegatta(baseEventName, boatClassName, regattaId, series,
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        Iterable<Waypoint> emptyWaypointList = Collections.emptyList();
        Competitor competitor = AbstractLeaderboardTest.createCompetitor("Der mit dem Kiel zieht");
        Iterable<Competitor> competitors = Collections.singleton(competitor);
        RaceDefinition raceDefinition = new RaceDefinitionImpl("Test Race", new CourseImpl("Empty Course", emptyWaypointList),
                masterRegatta.getBoatClass(), competitors);
        master.apply(new AddRaceDefinition(masterRegatta.getRegattaIdentifier(), raceDefinition));
        Thread.sleep(1000);
        Regatta replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertTrue(replicatedRegatta.isPersistent());
        assertTrue(Util.isEmpty((replicatedRegatta.getSeries())));
        assertNull(replicatedRegatta.getDefaultCourseArea());        
        assertTrue(regattaId.equals(replicatedRegatta.getId()));
    }
    
    @Test
    public void testUpdateSpecificRegattaReplication() throws InterruptedException {
        Regatta replicatedRegatta;
        
        final UUID alphaCourseAreaId = UUID.randomUUID();
        final UUID tvCourseAreaId = UUID.randomUUID();
        
        Event event = master.addEvent("Event", "Venue", ".", true, UUID.randomUUID());
        master.addCourseArea(event.getId(), "Alpha", alphaCourseAreaId);
        master.addCourseArea(event.getId(), "TV", tvCourseAreaId);
        
        UUID currentCourseAreaId = null;
        Regatta masterRegatta = master.createRegatta("Kiel Week 2012", "49er", UUID.randomUUID(), Collections.<Series>emptyList(),
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), currentCourseAreaId);
        
        // Test for 'null'
        master.updateRegatta(new RegattaName(masterRegatta.getName()), currentCourseAreaId);
        Thread.sleep(1000);
        replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertNull(replicatedRegatta.getDefaultCourseArea());
        
        // Test for 'alpha'
        currentCourseAreaId = alphaCourseAreaId;
        master.updateRegatta(new RegattaName(masterRegatta.getName()), currentCourseAreaId);
        Thread.sleep(1000);
        replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertEquals(currentCourseAreaId, replicatedRegatta.getDefaultCourseArea().getId());
        
        // Test for 'tv'
        currentCourseAreaId = tvCourseAreaId;
        master.updateRegatta(new RegattaName(masterRegatta.getName()), currentCourseAreaId);
        Thread.sleep(1000);
        replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertEquals(currentCourseAreaId, replicatedRegatta.getDefaultCourseArea().getId());
        
        // Test back to 'null'
        currentCourseAreaId = null;
        master.updateRegatta(new RegattaName(masterRegatta.getName()), currentCourseAreaId);
        Thread.sleep(1000);
        replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertNull(replicatedRegatta.getDefaultCourseArea());
    }

    @Test
    public void testDefaultRegattaReplication() throws InterruptedException {
        final String baseEventName = "Kiel Week 2012";
        final String boatClassName = "49er";
        final UUID regattaId = UUID.randomUUID();
        Regatta masterRegatta = master.getOrCreateDefaultRegatta(baseEventName, boatClassName, regattaId);
        Thread.sleep(1000);
        Regatta replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertTrue(regattaId.equals(replicatedRegatta.getId()));
    }
    
    @Test
    public void testSpecificRegattaReplicationWithTwoEmptySeries() throws InterruptedException {
        final String baseEventName = "Kiel Week 2012";
        final String boatClassName = "49er";
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();
        Series qualification = new SeriesImpl("Qualification", /* isMedal */ false,
                Arrays.asList(new Fleet[] { new FleetImpl("Yellow"), new FleetImpl("Blue") }), emptyRaceColumnNamesList, /* trackedRegattaRegistry */ null);
        Series finals = new SeriesImpl("Finals", /* isMedal */ false,
                Arrays.asList(new Fleet[] { new FleetImpl("Gold", 1), new FleetImpl("Silver", 2) }), emptyRaceColumnNamesList, /* trackedRegattaRegistry */ null);
        Series medal = new SeriesImpl("Medal", /* isMedal */ true,
                Arrays.asList(new Fleet[] { new FleetImpl("Medal") }), emptyRaceColumnNamesList, /* trackedRegattaRegistry */ null);
        Regatta masterRegatta = master.createRegatta(baseEventName, boatClassName,
                UUID.randomUUID(), Arrays.asList(new Series[] { qualification, finals, medal }), /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        Thread.sleep(1000);
        Regatta replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertTrue(replicatedRegatta.isPersistent());
        assertFalse(Util.isEmpty((replicatedRegatta.getSeries())));
        Iterator<? extends Series> seriesIter = replicatedRegatta.getSeries().iterator();
        Series replicatedQualification = seriesIter.next();
        assertEquals("Qualification", replicatedQualification.getName());
        assertEquals(2, Util.size(replicatedQualification.getFleets()));
        assertNotNull(replicatedQualification.getFleetByName("Yellow"));
        assertNotNull(replicatedQualification.getFleetByName("Blue"));
        assertEquals(0, replicatedQualification.getFleetByName("Yellow").compareTo(replicatedQualification.getFleetByName("Blue")));
        Series replicatedFinals = seriesIter.next();
        assertEquals("Finals", replicatedFinals.getName());
        assertEquals(2, Util.size(replicatedFinals.getFleets()));
        assertNotNull(replicatedFinals.getFleetByName("Silver"));
        assertNotNull(replicatedFinals.getFleetByName("Gold"));
        assertEquals(1, replicatedFinals.getFleetByName("Gold").getOrdering());
        assertEquals(2, replicatedFinals.getFleetByName("Silver").getOrdering());
        Series replicatedMedal = seriesIter.next();
        assertEquals("Medal", replicatedMedal.getName());
        assertEquals(1, Util.size(replicatedMedal.getFleets()));
        assertNotNull(replicatedMedal.getFleetByName("Medal"));
        assertNull(replicatedRegatta.getDefaultCourseArea());
    }
    
    @Test
    public void testSpecificRegattaReplicationWithCourseArea() throws InterruptedException {
        final String eventName = "ESS Singapur";
        final String venueName = "Singapur, Singapur";
        final String publicationUrl = "http://ess40.sapsailing.com";
        final boolean isPublic = false;
        final String boatClassName = "X40";
        final Iterable<Series> series = Collections.emptyList();
        final String courseArea = "Alpha";
        Event masterEvent = master.addEvent(eventName, venueName, publicationUrl, isPublic, UUID.randomUUID());
        CourseArea masterCourseArea = master.addCourseArea(masterEvent.getId(), courseArea, UUID.randomUUID());
        
        Regatta masterRegatta = master.createRegatta(eventName, boatClassName, UUID.randomUUID(), series,
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), masterCourseArea.getId());
        Thread.sleep(1000);
        Event replicatedEvent = replica.getEvent(masterEvent.getId());
        assertNotNull(replicatedEvent);
        CourseArea replicatedCourseArea = replica.getCourseArea(masterCourseArea.getId());
        assertNotNull(replicatedCourseArea);
        Regatta replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertTrue(replicatedRegatta.isPersistent());
        assertTrue(Util.isEmpty((replicatedRegatta.getSeries())));
        assertNotNull(replicatedRegatta.getDefaultCourseArea());
        assertEquals(masterCourseArea.getId(), replicatedRegatta.getDefaultCourseArea().getId());
        assertEquals(masterCourseArea.getName(), replicatedRegatta.getDefaultCourseArea().getName());
    }
}
