package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;

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
    public void testLoadStoreSimpleRegatta() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegatta(numberOfQualifyingRaces, numberOfFinalRaces, regattaBaseName, boatClass);
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName());
        assertEquals(regattaBaseName, loadedRegatta.getBaseName());
    }

    @Test
    public void testLoadStoreRegattaWithFleetsEnsuringIdenticalFleetsInSeriesAndRaceColumns() {
        
    }


    private Regatta createRegatta(final int numberOfQualifyingRaces, final int numberOfFinalRaces,
            final String regattaBaseName, BoatClass boatClass) {
        List<Series> series = new ArrayList<Series>();
        
        // -------- qualifying series ------------
        List<Fleet> qualifyingFleets = new ArrayList<Fleet>();
        qualifyingFleets.add(new FleetImpl("Yellow"));
        qualifyingFleets.add(new FleetImpl("Blue"));
        List<String> qualifyingRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfQualifyingRaces; i++) {
            qualifyingRaceColumnNames.add("Q"+i);
        }
        Series qualifyingSeries = new SeriesImpl("Qualifying", /* isFleetsOrdered */false, /* isMedal */false,
                qualifyingFleets, qualifyingRaceColumnNames);
        series.add(qualifyingSeries);
        
        // -------- final series ------------
        List<Fleet> finalFleets = new ArrayList<Fleet>();
        finalFleets.add(new FleetImpl("Gold"));
        finalFleets.add(new FleetImpl("Silver"));
        List<String> finalRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfFinalRaces; i++) {
            finalRaceColumnNames.add("F"+i);
        }
        Series finalSeries = new SeriesImpl("Final", /* isFleetsOrdered */ true, /* isMedal */ false, finalFleets, finalRaceColumnNames);
        series.add(finalSeries);

        // ------------ medal --------------
        List<Fleet> medalFleets = new ArrayList<Fleet>();
        medalFleets.add(new FleetImpl("Medal"));
        List<String> medalRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfFinalRaces; i++) {
            medalRaceColumnNames.add("M");
        }
        Series medalSeries = new SeriesImpl("Medal", /* isFleetsOrdered */ true, /* isMedal */ true, medalFleets, medalRaceColumnNames);
        series.add(medalSeries);

        Regatta regatta = new RegattaImpl(regattaBaseName, boatClass, series);
        return regatta;
    }
    
}
