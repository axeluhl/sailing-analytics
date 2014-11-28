package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceColumnInSeriesImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.leaderboard.EventResolver;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithFixedRank;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestStoringAndLoadingEventsAndRegattas extends AbstractMongoDBTest {
    private static final Logger logger = Logger.getLogger(TestStoringAndLoadingEventsAndRegattas.class.getName());
    
    public TestStoringAndLoadingEventsAndRegattas() throws UnknownHostException, MongoException {
        super();
    }
    
    private LeaderboardGroup createLeaderboardGroup(String name) {
        return new LeaderboardGroupImpl(name, "Description for "+name, /* displayName */ null, /* displayInReverseOrder */ false, Collections.<Leaderboard>emptyList());
    }

    @Test
    public void testLoadStoreSimpleEventWithLinkToLeaderboardGroups() throws MalformedURLException {
        final String eventName = "Event Name";
        final String eventDescription = "Event Description";
        final String venueName = "Venue Name";
        final String[] courseAreaNames = new String[] { "Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrott" };
        final Venue venue = new VenueImpl(venueName);
        
        Calendar cal = Calendar.getInstance();
        cal.set(2012, 12, 1);
        final TimePoint startDate = new MillisecondsTimePoint(cal.getTimeInMillis());
        cal.set(2012, 12, 5);
        final TimePoint endDate = new MillisecondsTimePoint(cal.getTimeInMillis());
        
        for (String courseAreaName : courseAreaNames) {
            CourseArea courseArea = DomainFactory.INSTANCE.getOrCreateCourseArea(UUID.randomUUID(), courseAreaName);
            venue.addCourseArea(courseArea);
        }
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        final Event event = new EventImpl(eventName, startDate, endDate, venue, /*isPublic*/ true, UUID.randomUUID());
        final LeaderboardGroup lg1 = createLeaderboardGroup("lg1");
        final LeaderboardGroup lg2 = createLeaderboardGroup("lg2");
        event.addLeaderboardGroup(lg1);
        event.addLeaderboardGroup(lg2);
        event.setDescription(eventDescription);
        event.addImageURL(new URL("http://some.host/with/some/file1.jpg"));
        event.addImageURL(new URL("http://some.host/with/some/file2.jpg"));
        event.addVideoURL(new URL("http://some.host/with/some/file1.mp4"));
        event.addVideoURL(new URL("http://some.host/with/some/file2.mp4"));
        event.addSponsorImageURL(new URL("http://some.host/with/some/file4.mp4"));
        event.addSponsorImageURL(new URL("http://some.host/with/some/file5.mp4"));
        event.setOfficialWebsiteURL(new URL("http://official.website.com"));
        event.setLogoImageURL(new URL("http://official.logo.com"));
        mof.storeEvent(event);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        final Event loadedEvent = dof.loadEvent(eventName);
        dof.loadLeaderboardGroupLinksForEvents(new EventResolver() {
            @Override
            public Event getEvent(Serializable id) {
                return id.equals(loadedEvent.getId()) ? loadedEvent : null;
            }
        }, new LeaderboardGroupResolver() {
            @Override
            public LeaderboardGroup getLeaderboardGroupByName(String leaderboardGroupName) {
                return leaderboardGroupName.equals(lg1.getName()) ? lg1 : leaderboardGroupName.equals(lg2.getName()) ? lg2 : null;
            }
            
            @Override
            public LeaderboardGroup getLeaderboardGroupByID(UUID leaderboardGroupID) {
                return leaderboardGroupID.equals(lg1.getId()) ? lg1 : leaderboardGroupID.equals(lg2.getId()) ? lg2 : null;
            }
        });
        assertNotNull(loadedEvent);
        assertEquals(eventName, loadedEvent.getName());
        assertEquals(eventDescription, loadedEvent.getDescription());
        assertEquals(event.getOfficialWebsiteURL(), loadedEvent.getOfficialWebsiteURL());
        assertEquals(event.getLogoImageURL(), loadedEvent.getLogoImageURL());
        assertEquals(2, Util.size(loadedEvent.getLeaderboardGroups()));
        Iterator<LeaderboardGroup> lgIter = loadedEvent.getLeaderboardGroups().iterator();
        assertSame(lg1, lgIter.next());
        assertSame(lg2, lgIter.next());
        final Venue loadedVenue = loadedEvent.getVenue();
        assertNotNull(loadedVenue);
        assertEquals(venueName, loadedVenue.getName());
        assertEquals(courseAreaNames.length, Util.size(loadedVenue.getCourseAreas()));
        int i=0;
        for (CourseArea loadedCourseArea : loadedVenue.getCourseAreas()) {
            assertEquals(courseAreaNames[i++], loadedCourseArea.getName());
        }
        assertTrue("image URLs "+loadedEvent.getImageURLs()+" but expected "+event.getImageURLs(), Util.equals(event.getImageURLs(), loadedEvent.getImageURLs()));
        assertTrue("video URLs "+loadedEvent.getVideoURLs()+" but expected "+event.getVideoURLs(), Util.equals(event.getVideoURLs(), loadedEvent.getVideoURLs()));
        assertTrue("sponsor image URLs "+loadedEvent.getSponsorImageURLs()+" but expected "+event.getSponsorImageURLs(), Util.equals(event.getSponsorImageURLs(), loadedEvent.getSponsorImageURLs()));
    }
    
    @Test
    public void testLoadStoreSimpleEventAndRegattaWithCourseArea() {
        final String eventName = "Event Name";
        final String venueName = "Venue Name";
        final String courseAreaName = "Alpha";
        final Venue venue = new VenueImpl(venueName);
        CourseArea courseArea = DomainFactory.INSTANCE.getOrCreateCourseArea(UUID.randomUUID(), courseAreaName);
        venue.addCourseArea(courseArea);

        Calendar cal = Calendar.getInstance();
        cal.set(2012, 12, 1);
        final TimePoint startDate = new MillisecondsTimePoint(cal.getTimeInMillis());
        cal.set(2012, 12, 5);
        final TimePoint endDate = new MillisecondsTimePoint(cal.getTimeInMillis());

        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        Event event = new EventImpl(eventName, startDate, endDate, venue, /*isPublic*/ true, UUID.randomUUID());
        mof.storeEvent(event);
        
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegatta(RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()), boatClass, 
                /*startDate*/ null, /*endDate*/ null, /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), courseArea);
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertNotNull(loadedRegatta);
        assertEquals(regatta.getName(), loadedRegatta.getName());
        assertEquals(Util.size(regatta.getSeries()), Util.size(loadedRegatta.getSeries()));
        assertNotNull(loadedRegatta.getDefaultCourseArea());
        assertEquals(loadedRegatta.getDefaultCourseArea().getId(), courseArea.getId());
        assertEquals(loadedRegatta.getDefaultCourseArea().getName(), courseArea.getName());
    }
    
    @Test
    public void testLoadStoreRegattaConfiguration() {
        
        RegattaConfigurationImpl configuration = new RegattaConfigurationImpl();
        configuration.setDefaultRacingProcedureType(RacingProcedureType.BASIC);
        
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("ESS40", false);
        Regatta regatta = createRegattaAndAddRaceColumns(1, 1, RegattaImpl.getDefaultName("RR", boatClass.getName()), boatClass, false, 
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.HIGH_POINT));
        regatta.setRegattaConfiguration(configuration);
        
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), null);
        
        assertNotNull(loadedRegatta.getRegattaConfiguration());
        assertEquals(RacingProcedureType.BASIC, loadedRegatta.getRegattaConfiguration().getDefaultRacingProcedureType());
    }

    @Test
    public void testLoadStoreSimpleRegattaLeaderboard() {
        RacingEventService res = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE), PersistenceFactory.INSTANCE
                .getMongoObjectFactory(getMongoService()), MediaDBFactory.INSTANCE.getMediaDB(getMongoService()), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regattaProxy = createRegatta(RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()), boatClass, 
                /*startDate*/ null, /*endDate*/ null, /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        final String regattaName = regattaProxy.getName();
        Regatta regatta = res.createRegatta(regattaName, regattaProxy.getBoatClass().getName(), /*startDate*/ null, /*endDate*/ null,
                "123", regattaProxy.getSeries(), regattaProxy.isPersistent(), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /* useStartTimeInference */ true);
        addRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regatta);
        res.addRegattaLeaderboard(regatta.getRegattaIdentifier(), null, new int[] { 3, 5 });
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
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
    public void testLoadStoreRegattaLeaderboardWithScoreCorrections() {
        // for some reason the dropping of collections doesn't work reliably on Linux... explicitly drop those collections that we depend on
        getMongoService().getDB().getCollection(CollectionNames.LEADERBOARDS.name()).drop();
        getMongoService().getDB().getCollection(CollectionNames.REGATTAS.name()).drop();
        Competitor hasso = AbstractLeaderboardTest.createCompetitor("Dr. Hasso Plattner");
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        final DynamicTrackedRegatta[] trackedRegatta = new DynamicTrackedRegatta[1];
        final DynamicTrackedRace q2YellowTrackedRace = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 1, /* started */ false, boatClass) {
            private static final long serialVersionUID = 1234L;
            @Override
            public RegattaAndRaceIdentifier getRaceIdentifier() {
                return new RegattaNameAndRaceName("Kieler Woche (29erXX)", "Yellow Race 2");
            }
            @Override
            public DynamicTrackedRegatta getTrackedRegatta() {
                return trackedRegatta[0];
            }
        };
        RacingEventService res = createRacingEventServiceWithOneMockedTrackedRace(q2YellowTrackedRace);
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        Calendar cal = Calendar.getInstance();
        cal.set(2012, 12, 1);
        final TimePoint startDate = new MillisecondsTimePoint(cal.getTimeInMillis());
        cal.set(2012, 12, 5);
        final TimePoint endDate = new MillisecondsTimePoint(cal.getTimeInMillis());
        
        Regatta regattaProxy = createRegatta(RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()), boatClass, startDate, endDate, /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        Regatta regatta = res.createRegatta(regattaProxy.getName(), regattaProxy.getBoatClass().getName(), regattaProxy.getStartDate(), regattaProxy.getEndDate(),
                "123", regattaProxy.getSeries(), regattaProxy.isPersistent(), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /* useStartTimeInference */ true);
        trackedRegatta[0] = new DynamicTrackedRegattaImpl(regatta);
        addRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regatta);
        logColumnsInRegatta(regatta);
        RegattaLeaderboard regattaLeaderboard = res.addRegattaLeaderboard(regatta.getRegattaIdentifier(), null, new int[] { 3, 5 });
        assertSame(regatta, regattaLeaderboard.getRegatta());
        final RaceColumnInSeries q2 = regatta.getSeriesByName("Qualifying").getRaceColumnByName("Q2");
        final Fleet yellow = q2.getFleetByName("Yellow");
        logColumnsInRegatta(regatta);
        logColumnsInRegattaLeaderboard(regattaLeaderboard);
        assertNotNull(regattaLeaderboard.getRaceColumnByName(q2.getName()));
        res.apply(new ConnectTrackedRaceToLeaderboardColumn(regattaLeaderboard.getName(), q2.getName(), yellow
                .getName(), q2YellowTrackedRace.getRaceIdentifier()));
        res.apply(new UpdateLeaderboardMaxPointsReason(regattaLeaderboard.getName(), q2.getName(), hasso.getId().toString(),
                MaxPointsReason.DNF, MillisecondsTimePoint.now()));
        
        // load new RacingEventService including regatta and leaderboard
        RacingEventService resForLoading = createRacingEventServiceWithOneMockedTrackedRace(q2YellowTrackedRace);
        Regatta loadedRegatta = resForLoading.getRegattaByName("Kieler Woche (29erXX)");
        assertNotNull(loadedRegatta);
        assertEquals(regatta.getName(), loadedRegatta.getName());
        assertEquals(Util.size(regatta.getSeries()), Util.size(loadedRegatta.getSeries()));
        Leaderboard loadedLeaderboard = resForLoading.getLeaderboardByName(loadedRegatta.getName());
        assertNotNull(loadedLeaderboard);
        assertEquals(((ThresholdBasedResultDiscardingRule) regattaLeaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces().length,
                ((ThresholdBasedResultDiscardingRule) loadedLeaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces().length);
        assertTrue(Arrays.equals(((ThresholdBasedResultDiscardingRule) regattaLeaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces(),
                ((ThresholdBasedResultDiscardingRule) loadedLeaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces()));
        assertTrue(loadedLeaderboard instanceof RegattaLeaderboard);
        RegattaLeaderboard loadedRegattaLeaderboard = (RegattaLeaderboard) loadedLeaderboard;
        assertSame(loadedRegatta, loadedRegattaLeaderboard.getRegatta());
        // now re-associate the tracked race to let score correction "snap" to competitor:
        final RaceColumnInSeries loadedQ2 = loadedRegatta.getSeriesByName("Qualifying").getRaceColumnByName("Q2");
        final Fleet loadedYellow = loadedQ2.getFleetByName("Yellow");
        // adjust tracked regatta for tracked race:
        trackedRegatta[0] = new DynamicTrackedRegattaImpl(loadedRegatta);
        resForLoading.apply(new ConnectTrackedRaceToLeaderboardColumn(loadedLeaderboard.getName(), loadedQ2.getName(), loadedYellow
                .getName(), q2YellowTrackedRace.getRaceIdentifier()));
        MaxPointsReason hassosLoadedMaxPointsReason = loadedLeaderboard.getScoreCorrection().getMaxPointsReason(hasso, loadedQ2, MillisecondsTimePoint.now());
        assertEquals(MaxPointsReason.DNF, hassosLoadedMaxPointsReason);
    }

    private void logColumnsInRegattaLeaderboard(RegattaLeaderboard regattaLeaderboard) {
        StringBuilder rlbrcNames = new StringBuilder();
        for (RaceColumn rlbrc : regattaLeaderboard.getRaceColumns()) {
            rlbrcNames.append("; ");
            rlbrcNames.append(rlbrc.getName());
        }
        logger.info("columns in regatta leaderboard for regatta "+regattaLeaderboard.getRegatta().getName()+" ("+
                regattaLeaderboard.getRegatta().hashCode()+"): "+rlbrcNames);
        logColumnsInRegatta(regattaLeaderboard.getRegatta());
    }

    private void logColumnsInRegatta(Regatta regatta) {
        StringBuilder rrcNames = new StringBuilder();
        for (Series series : regatta.getSeries()) {
            for (RaceColumn raceColumn : series.getRaceColumns()) {
                rrcNames.append("; ");
                rrcNames.append(raceColumn.getName());
            }
        }
        logger.info("columns in regatta "+regatta.getName()+" ("+regatta.hashCode()+") : "+rrcNames);
    }

    private RacingEventServiceImpl createRacingEventServiceWithOneMockedTrackedRace(final DynamicTrackedRace q2YellowTrackedRace) {
        return new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE), PersistenceFactory.INSTANCE
                .getMongoObjectFactory(getMongoService()), MediaDBFactory.INSTANCE.getMediaDB(getMongoService()), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE) {
            @Override
            public DynamicTrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier raceIdentifier) {
                return q2YellowTrackedRace;
            }
        };
    }
    
    @Test
    public void testLoadStoreSimpleRegatta() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */true);
        final String regattaName = RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName());
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, regattaName,
                boatClass, /* persistent */false,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertSame(LowPoint.class, loadedRegatta.getScoringScheme().getClass());
        assertEquals(regattaName, loadedRegatta.getName());
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
    public void testLoadStoreSimpleRegattaWithSeriesScoringScheme() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()),
                boatClass, /* persistent */false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        regatta.getSeriesByName("Qualifying").setResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(new int[] { 1, 2, 3 }));
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertTrue(Arrays.equals(new int[] { 1, 2, 3 },
                loadedRegatta.getSeriesByName("Qualifying").getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces()));
    }

    @Test
    public void testLoadStoreSimpleRegattaWithScoreForMedalStartingWithZero() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()),
                boatClass, /* persistent */false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        regatta.getSeriesByName("Medal").setStartsWithZeroScore(true);
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertFalse(loadedRegatta.getSeriesByName("Qualifying").isStartsWithZeroScore());
        assertTrue(loadedRegatta.getSeriesByName("Medal").isStartsWithZeroScore());
    }

    @Test
    public void testLoadStoreSimpleRegattaWithHighPointScoringScheme() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "ESS40 Cardiff 2012";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("ESS40", /* typicallyStartsUpwind */ false);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces, RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()),
                boatClass, /* persistent */false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.HIGH_POINT));
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertSame(HighPoint.class, loadedRegatta.getScoringScheme().getClass());
    }

    @Test
    public void testLoadStoreRegattaWithFleetsEnsuringIdenticalFleetsInSeriesAndRaceColumns() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        final String regattaName = RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName());
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces,
                regattaName, boatClass,
                /* persistent */false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertEquals(regattaName, loadedRegatta.getName());
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
    public void testLoadStoreRegattaWithFleetsEnsuringFleetOrdering() {
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        final String regattaName = RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName());
        Regatta regatta = createRegatta(regattaName, boatClass, /*startDate*/ null, /*endDate*/ null,
                /* persistent */ false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
        Regatta loadedRegatta = dof.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        assertEquals(regattaName, loadedRegatta.getName());

        Iterator<? extends Series> seriesIter = loadedRegatta.getSeries().iterator();
        Series loadedQualifyingSeries = seriesIter.next();
        
        Iterator<? extends Fleet> qualiFleetIt = loadedQualifyingSeries.getFleets().iterator();
        Fleet qualiFleet1 = qualiFleetIt.next();
        assertEquals(qualiFleet1.getName(), "Yellow");
        Fleet qualiFleet2 = qualiFleetIt.next();
        assertEquals(qualiFleet2.getName(), "Blue");
        
        Series loadedFinalSeries = seriesIter.next();
        Iterator<? extends Fleet> finalFleetIt = loadedFinalSeries.getFleets().iterator();
        Fleet finalFleet1 = finalFleetIt.next();
        assertEquals(finalFleet1.getName(), "Gold");
        Fleet finalFleet2 = finalFleetIt.next();
        assertEquals(finalFleet2.getName(), "Silver");
    }

    @Test
    public void testStorageOfRaceIdentifiersOnRaceColumnInSeries() {
        final int numberOfQualifyingRaces = 5;
        final int numberOfFinalRaces = 7;
        final String regattaBaseName = "Kieler Woche";
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegattaAndAddRaceColumns(numberOfQualifyingRaces, numberOfFinalRaces,
                RegattaImpl.getDefaultName(regattaBaseName, boatClass.getName()), boatClass,
                /* persistent */false, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        Series qualifyingSeries = regatta.getSeries().iterator().next();
        RaceColumn q2 = qualifyingSeries.getRaceColumnByName("Q2");
        final RegattaNameAndRaceName q2TrackedRaceIdentifier = new RegattaNameAndRaceName(regatta.getName(), "Q2 TracTrac");
        q2.setRaceIdentifier(qualifyingSeries.getFleetByName("Yellow"), q2TrackedRaceIdentifier);
        MongoObjectFactory mof = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        mof.storeRegatta(regatta);
        
        DomainObjectFactory dof = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);
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
            final String regattaName, BoatClass boatClass, boolean persistent, ScoringScheme scoringScheme) {
        Regatta regatta = createRegatta(regattaName, boatClass, /*startDate*/ null, /*endDate*/ null, persistent, scoringScheme, null);
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

    private Regatta createRegatta(final String regattaName, BoatClass boatClass, TimePoint startDate, TimePoint endDate, boolean persistent, ScoringScheme scoringScheme, CourseArea courseArea) {
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
        Regatta regatta = new RegattaImpl(regattaName, boatClass, startDate, endDate, series, persistent, scoringScheme, "123", courseArea);
        return regatta;
    }

    private void addRaceColumnsToSeries(List<String> finalRaceColumnNames, Series finalSeries) {
        for (String raceColumnName : finalRaceColumnNames) {
            finalSeries.addRaceColumn(raceColumnName, /* trackedRegattaRegistry */ null);
        }
    }
    
    @Test
    public void testRegattaRaceAssociationStore() throws Exception {
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("112er", /* typicallyStartsUpwind */ true);
        Regatta regatta = createRegatta(RegattaImpl.getDefaultName("Cologne Masters", boatClass.getName()), boatClass, 
                /*startDate*/ null, /*endDate*/ null, /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);

        List<Competitor> competitors = new ArrayList<Competitor>();
        competitors.add(new CompetitorImpl("Axel", "Axel Uhl", Color.RED, null, null));
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Course", waypoints);
        
        RaceDefinition racedef = new RaceDefinitionImpl("M1", course, boatClass, competitors);
        regatta.addRace(racedef);
        
        RacingEventServiceImpl evs = new RacingEventServiceImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE), PersistenceFactory.INSTANCE
                .getMongoObjectFactory(getMongoService()), MediaDBFactory.INSTANCE.getMediaDB(getMongoService()), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE);
        assertNull(evs.getRememberedRegattaForRace(racedef.getId()));
        evs.raceAdded(regatta, racedef);
        assertNotNull(evs.getRememberedRegattaForRace(racedef.getId()));
        evs.removeRegatta(regatta);
        assertNull(evs.getRememberedRegattaForRace(racedef.getId()));
    }
    
}
