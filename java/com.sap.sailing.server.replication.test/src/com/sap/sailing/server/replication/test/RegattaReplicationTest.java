package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util;

public class RegattaReplicationTest extends AbstractServerReplicationTest {
    @Test
    public void testSimpleSpecificRegattaReplication() throws InterruptedException {
        final String baseEventName = "Kiel Week 2012";
        final String boatClassName = "49er";
        final Iterable<Series> series = Collections.emptyList();
        Regatta masterRegatta = master.createRegatta(baseEventName, boatClassName, UUID.randomUUID(), series,
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        Thread.sleep(1000);
        Regatta replicatedRegatta = replica.getRegatta(new RegattaName(masterRegatta.getName()));
        assertNotNull(replicatedRegatta);
        assertTrue(replicatedRegatta.isPersistent());
        assertTrue(Util.isEmpty((replicatedRegatta.getSeries())));
        assertNull(replicatedRegatta.getDefaultCourseArea());
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
        List<String> regattas = new ArrayList<String>();

        final String courseArea = "Alpha";

        Event masterEvent = master.addEvent(eventName, venueName, publicationUrl, isPublic, UUID.randomUUID(), regattas);
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
