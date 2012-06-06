package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceColumnInSeriesImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class TestStoringAndLoadingEventsAndRegattas extends AbstractMongoDBTest {
    @Test
    public void testLoadStoreSimpleEvent() {
        final String eventName = "Event Name";
        final String venueName = "Venue Name";
        final String[] courseAreaNames = new String[] { "Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrott" };
        final Venue venue = new VenueImpl(venueName);
        for (String courseAreaName : courseAreaNames) {
            CourseArea courseArea = new CourseAreaImpl(courseAreaName);
            venue.addCourseArea(courseArea);
        }
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        Event event = new EventImpl(eventName, venue);
        mof.storeEvent(event);
        
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Event loadedEvent = dof.loadEvent(eventName);
        assertNotNull(loadedEvent);
        assertEquals(eventName, loadedEvent.getName());
        final Venue loadedVenue = loadedEvent.getVenue();
        assertNotNull(loadedVenue);
        assertEquals(venueName, loadedVenue.getName());
        assertEquals(courseAreaNames.length, Util.size(loadedVenue.getCourseAreas()));
        int i=0;
        for (CourseArea loadedCourseArea : loadedVenue.getCourseAreas()) {
            assertEquals(courseAreaNames[i++], loadedCourseArea.getName());
        }
    }

    @Test
    public void testLoadStoreSimpleRegattaLeaderboard() {
        RacingEventService res = new RacingEventServiceImpl(getMongoService());
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regattaProxy = createRegatta(regattaBaseName, boatClass, /* persistent */ true);
        Regatta regatta = res.createRegatta(regattaProxy.getBaseName(), regattaProxy.getBoatClass().getName(),
                regattaProxy.getSeries(), regattaProxy.isPersistent());
        addRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regatta);
        res.addRegattaLeaderboard(regatta.getRegattaIdentifier(), new int[] { 3, 5 });
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertNotNull(loadedRegatta);
        assertEquals(regatta.getName(), loadedRegatta.getName());
        assertEquals(Util.size(regatta.getSeries()), Util.size(loadedRegatta.getSeries()));
        
        Leaderboard loadedLeaderboard = dof.loadLeaderboard(regatta.getName(), res);
        assertNotNull(loadedLeaderboard);
        assertTrue(loadedLeaderboard instanceof RegattaLeaderboard);
        RegattaLeaderboard loadedRegattaLeaderboard = (RegattaLeaderboard) loadedLeaderboard;
        assertSame(regatta, loadedRegattaLeaderboard.getRegatta());
    }
    
    @Test
    public void testLoadStoreSimpleRegatta() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regattaBaseName,
                boatClass, /* persistent */false);
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertEquals(regattaBaseName, loadedRegatta.getBaseName());
        Iterator<? extends Series> seriesIter = loadedRegatta.getSeries().iterator();
        Series loadedQualifyingSeries = seriesIter.next();
        assertEquals(numberOfQualifyingRaces, Util.size(loadedQualifyingSeries.getRaceColumns()));
        assertEquals(0, loadedQualifyingSeries.getFleetByName("Yellow").compareTo(loadedQualifyingSeries.getFleetByName("Blue")));
        Series loadedFinalSeries = seriesIter.next();
        assertEquals(numberOfFinalRaces, Util.size(loadedFinalSeries.getRaceColumns()));
        assertTrue(loadedFinalSeries.getFleetByName("Silver").compareTo(loadedFinalSeries.getFleetByName("Gold")) > 0);
        Series loadedMedalSeries = seriesIter.next();
        assertEquals(1, Util.size(loadedMedalSeries.getRaceColumns()));
    }

    @Test
    public void testLoadStoreRegattaWithFleetsEnsuringIdenticalFleetsInSeriesAndRaceColumns() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regattaBaseName, boatClass,
                /* persistent */ false);
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertEquals(regattaBaseName, loadedRegatta.getBaseName());
        Iterator<? extends Series> seriesIter = loadedRegatta.getSeries().iterator();
        Series loadedQualifyingSeries = seriesIter.next();
        int i=1;
        for (RaceColumn raceColumn : loadedQualifyingSeries.getRaceColumns()) {
            assertTrue(raceColumn instanceof RaceColumnInSeriesImpl);
            assertEquals("Q"+i, raceColumn.getName());
            assertTrue(Util.equals(loadedQualifyingSeries.getFleets(), raceColumn.getFleets()));
            i++;
        }
        Series loadedFinalSeries = seriesIter.next();
        i=1;
        for (RaceColumn raceColumn : loadedFinalSeries.getRaceColumns()) {
            assertTrue(raceColumn instanceof RaceColumnInSeriesImpl);
            assertEquals("F"+i, raceColumn.getName());
            assertTrue(Util.equals(loadedFinalSeries.getFleets(), raceColumn.getFleets()));
            i++;
        }
        Series loadedMedalSeries = seriesIter.next();
        for (RaceColumn raceColumn : loadedMedalSeries.getRaceColumns()) {
            assertTrue(raceColumn instanceof RaceColumnInSeriesImpl);
            assertEquals("M", raceColumn.getName());
            assertTrue(Util.equals(loadedMedalSeries.getFleets(), raceColumn.getFleets()));
        }
    }

    @Test
    public void testStorageOfRaceIdentifiersOnRaceColumnInSeries() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regattaBaseName, boatClass,
                /* persistent */ false);
        Series qualifyingSeries = regatta.getSeries().iterator().next();
        RaceColumn q2 = qualifyingSeries.getRaceColumnByName("Q2");
        final RegattaNameAndRaceName q2TrackedRaceIdentifier = new RegattaNameAndRaceName(regatta.getName(), "Q2 TracTrac");
        q2.setRaceIdentifier(qualifyingSeries.getFleetByName("Yellow"), q2TrackedRaceIdentifier);
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        Series loadedQualifyingSeries = loadedRegatta.getSeries().iterator().next();
        RaceColumn loadedQ2 = loadedQualifyingSeries.getRaceColumnByName("Q2");
        RaceIdentifier loadedQ2TrackedRaceIdentifier = loadedQ2.getRaceIdentifier(loadedQualifyingSeries.getFleetByName("Yellow"));
        assertEquals(q2TrackedRaceIdentifier, loadedQ2TrackedRaceIdentifier);
        assertNotSame(q2TrackedRaceIdentifier, loadedQ2TrackedRaceIdentifier);
        assertNull(loadedQualifyingSeries.getRaceColumnByName("Q1").getRaceIdentifier(loadedQualifyingSeries.getFleetByName("Yellow")));
        assertNull(loadedQualifyingSeries.getRaceColumnByName("Q2").getRaceIdentifier(loadedQualifyingSeries.getFleetByName("Blue")));
    }

    private Regatta createRegattaAndAddRaceColumns(final int numberOfQualifyingRaces, final int numberOfFinalRaces,
            final String regattaBaseName, BoatClass boatClass, boolean persistent) {
        Regatta regatta = createRegatta(regattaBaseName, boatClass, persistent);
        addRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regatta);
        return regatta;
    }

    private void addRaceColumns(final int numberOfQualifyingRaces, final int numberOfFinalRaces, Regatta regatta) {
        List<String> finalRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfFinalRaces; i++) {
            finalRaceColumnNames.add("F"+i);
        }
        List<String> qualifyingRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfQualifyingRaces; i++) {
            qualifyingRaceColumnNames.add("Q"+i);
        }
        List<String> medalRaceColumnNames = new ArrayList<String>();
        medalRaceColumnNames.add("M");
        addRaceColumnsToSeries(qualifyingRaceColumnNames, regatta.getSeriesByName("Qualifying"));
        addRaceColumnsToSeries(finalRaceColumnNames, regatta.getSeriesByName("Final"));
        addRaceColumnsToSeries(medalRaceColumnNames, regatta.getSeriesByName("Medal"));
    }

    private Regatta createRegatta(final String regattaBaseName, BoatClass boatClass, boolean persistent) {
        List<String> emptyRaceColumnNames = Collections.emptyList();
        List<Series> series = new ArrayList<Series>();
        
        // -------- qualifying series ------------
        List<Fleet> qualifyingFleets = new ArrayList<Fleet>();
        qualifyingFleets.add(new FleetImpl("Yellow"));
        qualifyingFleets.add(new FleetImpl("Blue"));
        Series qualifyingSeries = new SeriesImpl("Qualifying", /* isMedal */false, qualifyingFleets,
                emptyRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(qualifyingSeries);
        
        // -------- final series ------------
        List<Fleet> finalFleets = new ArrayList<Fleet>();
        finalFleets.add(new FleetImpl("Gold", 1));
        finalFleets.add(new FleetImpl("Silver", 2));
        Series finalSeries = new SeriesImpl("Final", /* isMedal */ false, finalFleets, emptyRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(finalSeries);

        // ------------ medal --------------
        List<Fleet> medalFleets = new ArrayList<Fleet>();
        medalFleets.add(new FleetImpl("Medal"));
        Series medalSeries = new SeriesImpl("Medal", /* isMedal */ true, medalFleets, emptyRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(medalSeries);
        Regatta regatta = new RegattaImpl(regattaBaseName, boatClass, series, persistent);
        return regatta;
    }

    private void addRaceColumnsToSeries(List<String> finalRaceColumnNames, Series finalSeries) {
        for (String raceColumnName : finalRaceColumnNames) {
            finalSeries.addRaceColumn(raceColumnName, /* trackedRegattaRegistry */ null);
        }
    }
    
}
