package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.masterdata.DummyTrackedRace;
import com.sap.sailing.server.masterdata.MasterDataImporter;

public class MasterDataImportTest {

    private static final String TEST_GROUP_NAME = "testGroup";
    private static final String TEST_EVENT_NAME = "testEvent";
    private static final String TEST_REGATTA_NAME = "testRegatta";
    private static final String TEST_BOAT_CLASS_NAME = "29er";
    private static final String TEST_LEADERBOARD_NAME = "testRegatta (29er)";
    private static final String TEST_GROUP_NAME2 = "testGroup2";

    private final UUID eventUUID = UUID.randomUUID();
    private RaceLogEventAuthor author = new RaceLogEventAuthorImpl("Test Author", 1);

    /**
     * Log Events created when running test. Will be removed from db at teardown
     */
    private Set<Serializable> storedLogUUIDs = new HashSet<Serializable>();

    @After
    public void tearDown() throws MalformedURLException, IOException, InterruptedException {
        deleteCreatedDataFromDatabase();

    }

    private void deleteCreatedDataFromDatabase() throws MalformedURLException, IOException, InterruptedException {
        storedLogUUIDs.clear();
        RacingEventService service = new RacingEventServiceImpl();
        LeaderboardGroup group = service.getLeaderboardGroupByName(TEST_GROUP_NAME);
        if (group != null) {
            service.removeLeaderboardGroup(TEST_GROUP_NAME);
        }
        LeaderboardGroup group2 = service.getLeaderboardGroupByName(TEST_GROUP_NAME2);
        if (group2 != null) {
            service.removeLeaderboardGroup(TEST_GROUP_NAME2);
        }
        Leaderboard leaderboard = service.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        if (leaderboard != null) {
            service.removeLeaderboard(TEST_LEADERBOARD_NAME);
        }
        Event event = service.getEvent(eventUUID);
        if (event != null) {
            service.removeEvent(eventUUID);
        }
        Regatta regatta = service.getRegattaByName(TEST_LEADERBOARD_NAME);
        if (regatta != null) {
            service.removeRegatta(regatta);
        }
        DBCollection raceLogCollection = MongoDBService.INSTANCE.getDB().getCollection("RACE_LOGS");
        // Removes all race log events
        DBCursor cursor = raceLogCollection.find();
        while (cursor.hasNext()) {
            raceLogCollection.remove(cursor.next());
        }
        // This should only delete those logs created during this test. Sadly it doesn't seem to work.
        // Didnt use CollectionNames stuff since it was not visible in this package. Sucks as soon as these names
        // change, I know..
        // raceLogCollection.ensureIndex(new BasicDBObject("RACE_LOG_EVENT_ID", null));
        // for (Serializable id : storedLogUUIDs) {
        // BasicDBObject query = new BasicDBObject();
        // query.put("RACE_LOG_EVENT.RACE_LOG_EVENT_ID", id);
        // DBCursor result = raceLogCollection.find(query);
        // while (result.hasNext()) {
        // raceLogCollection.remove(result.next());
        // }
        // }

        // Remove all media tracks
        Collection<MediaTrack> tracks = service.getAllMediaTracks();
        for (MediaTrack track : tracks) {
            service.mediaTrackDeleted(track);
        }
    }

    @Test
    public void testMasterDataImportWithoutHttpStack() throws MalformedURLException, IOException, InterruptedException,
            ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        String testFleet1Name = "testFleet1";
        FleetImpl testFleet1 = new FleetImpl(testFleet1Name);
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }
        double factor = 3.0;
        series.get(0).getRaceColumnByName(raceColumnName).setFactor(factor);

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        List<Competitor> competitors = new ArrayList<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        TimePoint logTimePoint2 = new MillisecondsTimePoint(1372489201000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        Position p = new DegreePosition(3, 3);
        Wind wind = new WindImpl(p, logTimePoint2, new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(12)));
        RaceLogWindFixEvent windEvent = factory.createWindFixEvent(logTimePoint2, author, UUID.randomUUID(),
                new ArrayList<Competitor>(), 2, wind);
        raceColumn.getRaceLog(testFleet1).add(windEvent);
        storedLogUUIDs.add(logEvent.getId());
        storedLogUUIDs.add(windEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);


            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }


        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);


        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);
        Assert.assertNull(raceColumnOnTarget.getTrackedRace(raceColumnOnTarget.getFleetByName(testFleet1Name)));

        raceColumnOnTarget.setTrackedRace(raceColumnOnTarget.getFleets().iterator().next(), new DummyTrackedRace(
                competitors, regattaOnTarget, null));

        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        Competitor competitorOnTarget = domainFactory.getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(competitorsCreatedOnTarget, regattaOnTarget, null);

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        Assert.assertEquals(factor, raceColumnOnTarget.getFactor());

        Iterable<Competitor> competitorsOnTarget = leaderboardOnTarget.getAllCompetitors();
        Iterator<Competitor> competitorIterator = competitorsOnTarget.iterator();
        Assert.assertTrue(competitorIterator.hasNext());
        Assert.assertEquals(competitorOnTarget, competitorIterator.next());

        // Check for score corrections
        Assert.assertEquals(
                scoreCorrection,
                leaderboardOnTarget.getScoreCorrection().getExplicitScoreCorrection(competitorOnTarget,
                        raceColumnOnTarget));
        Assert.assertEquals(
                maxPointsReason,
                leaderboardOnTarget.getScoreCorrection().getMaxPointsReason(competitorOnTarget, raceColumnOnTarget,
                        MillisecondsTimePoint.now()));

        // Check for carried points
        Assert.assertEquals(carriedPoints, leaderboardOnTarget.getCarriedPoints(competitorOnTarget));

        // Check for suppressed competitor
        Assert.assertTrue(leaderboardOnTarget.getSuppressedCompetitors().iterator().hasNext());
        Competitor suppressedCompetitorOnTarget = domainFactory.getCompetitorStore().getExistingCompetitorById(
                competitorToSuppressUUID);
        Assert.assertEquals(suppressedCompetitorOnTarget, leaderboardOnTarget.getSuppressedCompetitors().iterator()
                .next());

        // Check for competitor desplay name
        Assert.assertEquals(nickName, leaderboardOnTarget.getDisplayName(suppressedCompetitorOnTarget));

        // Check for race log event
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFixAtOrAfter(logTimePoint));
        Assert.assertEquals(logEvent.getId(),
                raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFixAtOrAfter(logTimePoint).getId());
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstFixAtOrAfter(logTimePoint2));
        Assert.assertEquals(wind, ((RaceLogWindFixEvent) raceColumnOnTarget.getRaceLog(fleet1OnTarget)
                .getFirstFixAtOrAfter(logTimePoint2)).getWindFix());

        // Check for persisting of race log events:
        RacingEventService dest2 = new RacingEventServiceImplMock();
        Leaderboard lb2 = dest2.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        RaceColumn raceColumn2 = lb2.getRaceColumns().iterator().next();
        RaceLog raceLog2 = raceColumn2.getRaceLog(raceColumn2.getFleetByName(fleet1OnTarget.getName()));
        Assert.assertEquals(logEvent.getId(), raceLog2.getFirstRawFixAtOrAfter(logTimePoint).getId());
    }

    @Test
    public void testMasterDataImportForScoreCorrections() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitor2UUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Test Mustermann", new NationalityImpl("GER"), new Date(645487200000L), "desc"));
        Person coach2 = new PersonImpl("Max Test", new NationalityImpl("GER"), new Date(645487200000L), "desc");
        DynamicTeam team2 = new TeamImpl("Pros2", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("FastBoat", boatClass, "GER70133");
        CompetitorImpl competitor2 = new CompetitorImpl(competitor2UUID, "Froderik", Color.RED, team2, boat2);
        competitors.add(competitor2);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);

        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);

        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        Competitor competitorOnTarget = domainFactory.getExistingCompetitorById(competitorUUID);
        Competitor competitorOnTarget2 = domainFactory.getCompetitorStore().getExistingCompetitorById(competitor2UUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(competitorsCreatedOnTarget, regattaOnTarget, null);

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        Iterable<Competitor> competitorsOnTarget = leaderboardOnTarget.getAllCompetitors();
        Iterator<Competitor> competitorIterator = competitorsOnTarget.iterator();
        Assert.assertTrue(competitorIterator.hasNext());
        Assert.assertEquals(competitorOnTarget, competitorIterator.next());

        // Check for score corrections
        Assert.assertEquals(
                scoreCorrection,
                leaderboardOnTarget.getScoreCorrection().getExplicitScoreCorrection(competitorOnTarget,
                        raceColumnOnTarget));
        Assert.assertEquals(
                maxPointsReason,
                leaderboardOnTarget.getScoreCorrection().getMaxPointsReason(competitorOnTarget, raceColumnOnTarget,
                        MillisecondsTimePoint.now()));

        // Checks if score correction was not set if not set on source
        Assert.assertFalse(leaderboardOnTarget.getScoreCorrection().isScoreCorrected(competitorOnTarget2,
                raceColumnOnTarget, MillisecondsTimePoint.now()));
    }

    @Test
    public void testMasterDataImportForWind() throws MalformedURLException, IOException, InterruptedException,
            ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta("testRegatta", "29er", regattaUUID, series, true, new LowPoint(),
                courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        DynamicPerson coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);

        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);

        Competitor competitorOnTarget = domainFactory.getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(competitorsCreatedOnTarget, regattaOnTarget, null);

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        // TODO Somehow have check if wind was really imported. DummyTrackedRace not sufficient

    }

    @Test
    public void testMasterDataImportForRaceLogEventsReferencingCompetitors() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        DomainFactory sourceDomainFactory = new DomainFactoryImpl();
        RacingEventService sourceService = new RacingEventServiceImpl(
                PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, sourceDomainFactory),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(MongoDBService.INSTANCE),
                MediaDBFactory.INSTANCE.getDefaultMediaDB(), EmptyWindStore.INSTANCE);
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        Competitor competitor = sourceDomainFactory.getOrCreateCompetitor(competitorUUID, "Froderik", Color.RED, team,
                boat);
        competitors.add(competitor);
        UUID competitor2UUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Test Mustermann", new NationalityImpl("GER"), new Date(645487200000L), "desc"));
        Person coach2 = new PersonImpl("Max Test", new NationalityImpl("GER"), new Date(645487200000L), "desc");
        DynamicTeam team2 = new TeamImpl("Pros2", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("FastBoat", boatClass, "GER70133");
        Competitor competitor2 = sourceDomainFactory.getCompetitorStore().getOrCreateCompetitor(competitor2UUID,
                "Froderik", Color.RED, team2, boat2);
        competitors.add(competitor2);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Add a competitor-related race log event to ensure that no competitor resolution is attempted while receiving
        TimePoint logTimePoint2 = logTimePoint.plus(10);
        CompetitorResults positionedCompetitors = new CompetitorResultsImpl();
        positionedCompetitors.add(new Triple<Serializable, String, MaxPointsReason>(competitor.getId(), competitor
                .getName(), MaxPointsReason.DNS));
        positionedCompetitors.add(new Triple<Serializable, String, MaxPointsReason>(competitor2.getId(), competitor2
                .getName(), MaxPointsReason.NONE));
        RaceLogFinishPositioningConfirmedEvent finishPositioningConfirmedEvent = factory
                .createFinishPositioningConfirmedEvent(logTimePoint2, author, 1, positionedCompetitors);
        raceColumn.getRaceLog(testFleet1).add(finishPositioningConfirmedEvent);
        storedLogUUIDs.add(finishPositioningConfirmedEvent.getId());

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);

        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);

        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        Competitor competitorOnTarget = domainFactory.getCompetitorStore().getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(competitorsCreatedOnTarget, regattaOnTarget, null);

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        Iterable<Competitor> competitorsOnTarget = leaderboardOnTarget.getAllCompetitors();
        Iterator<Competitor> competitorIterator = competitorsOnTarget.iterator();
        Assert.assertTrue(competitorIterator.hasNext());
        Assert.assertEquals(competitorOnTarget, competitorIterator.next());

        // Check for score corrections
        Assert.assertEquals(
                MaxPointsReason.DNS,
                leaderboardOnTarget.getScoreCorrection().getMaxPointsReason(competitorOnTarget, raceColumnOnTarget,
                        MillisecondsTimePoint.now()));
    }

    @Test
    public void testMasterDataImportWithoutOverrideWithoutHttpStack() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);



        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        RacingEventServiceImplMock destService;
        String venueNameNotToOverride;
        CourseAreaImpl courseAreaNotToOverride;
        RegattaLeaderboard leaderboardNotToOverride;
        LeaderboardGroup groupNotToOverride;
        String raceColumnNameNotToOveride;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            // Create existing data on target
            venueNameNotToOverride = "doNotOverride";
            Event eventNotToOverride = destService.addEvent(TEST_EVENT_NAME, venueNameNotToOverride, "", false,
                    eventUUID);
            courseAreaNotToOverride = new CourseAreaImpl("testAreaNotToOverride", courseAreaUUID);
            eventNotToOverride.getVenue().addCourseArea(courseAreaNotToOverride);

            List<String> raceColumnNamesNotToOverride = new ArrayList<String>();
            raceColumnNameNotToOveride = "T1nottooverride";
            raceColumnNamesNotToOverride.add(raceColumnNameNotToOveride);
            emptyRaceColumnNamesList = Collections.emptyList();

            List<Series> seriesNotToOverride = new ArrayList<Series>();
            List<Fleet> fleetsNotToOverride = new ArrayList<Fleet>();
            FleetImpl testFleet1NotToOverride = new FleetImpl("testFleet1");
            fleetsNotToOverride.add(testFleet1NotToOverride);
            seriesNotToOverride.add(new SeriesImpl("testSeries", false, fleetsNotToOverride, emptyRaceColumnNamesList,
                    destService));
            Regatta regattaNotToOverride = destService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME,
                    regattaUUID, seriesNotToOverride, true, new LowPoint(), courseAreaUUID);
            event.addRegatta(regattaNotToOverride);
            for (String name : raceColumnNamesNotToOverride) {
                seriesNotToOverride.get(0).addRaceColumn(name, destService);
            }

            leaderboardNotToOverride = destService.addRegattaLeaderboard(
                    regattaNotToOverride.getRegattaIdentifier(), "testDisplayNameNotToOverride", discardRule);
            List<String> leaderboardNamesNotToOverride = new ArrayList<String>();
            leaderboardNamesNotToOverride.add(leaderboardNotToOverride.getName());
            groupNotToOverride = destService.addLeaderboardGroup(TEST_GROUP_NAME,
                    "testGroupDescNotToOverride", false, leaderboardNamesNotToOverride, null, null);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);

        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // ---Asserts---
        // Test correct number of creations
        Assert.assertNotNull(creationCount);
        Assert.assertEquals(0, creationCount.getEventCount());
        Assert.assertEquals(0, creationCount.getRegattaCount());
        Assert.assertEquals(0, creationCount.getLeaderboardCount());
        Assert.assertEquals(0, creationCount.getLeaderboardGroupCount());

        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);

        // Check if existing event survived import
        Assert.assertEquals(venueNameNotToOverride, eventOnTarget.getVenue().getName());

        // Check if existing course area survived import
        Assert.assertEquals(courseAreaNotToOverride.getName(), eventOnTarget.getVenue().getCourseAreas().iterator()
                .next().getName());
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        // Check if existing leaderboard group survived import
        Assert.assertEquals(groupNotToOverride.getDescription(), leaderboardGroupOnTarget.getDescription());
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnNameNotToOveride);
        Assert.assertNotNull(raceColumnOnTarget);
        // Check if existing leaderboard survived import
        Assert.assertEquals(leaderboardNotToOverride.getDisplayName(), leaderboardOnTarget.getDisplayName());
        Assert.assertFalse(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));

    }

    @Test
    public void testMasterDataImportWithOverrideWithoutHttpStack() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        RegattaAndRaceIdentifier identifierOfRegattaTrackedRace;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            // Create existing data on target
            String venueNameToOverride = "Override";
            Event eventToOverride = destService.addEvent(TEST_EVENT_NAME, venueNameToOverride, "", false, eventUUID);
            CourseArea courseAreaToOverride = new CourseAreaImpl("testAreaToOverride", courseAreaUUID);
            eventToOverride.getVenue().addCourseArea(courseAreaToOverride);

            List<String> raceColumnNamesToOverride = new ArrayList<String>();
            String raceColumnNameToOveride = raceColumnName;
            raceColumnNamesToOverride.add(raceColumnNameToOveride);
            emptyRaceColumnNamesList = Collections.emptyList();

            List<Series> seriesToOverride = new ArrayList<Series>();
            List<Fleet> fleetsToOverride = new ArrayList<Fleet>();
            FleetImpl testFleet1ToOverride = new FleetImpl("testFleet1");
            fleetsToOverride.add(testFleet1ToOverride);
            seriesToOverride.add(new SeriesImpl("testSeries", false, fleetsToOverride, emptyRaceColumnNamesList,
                    destService));
            Regatta regattaToOverride = destService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID,
                    seriesToOverride, true, new LowPoint(), courseAreaUUID);
            event.addRegatta(regattaToOverride);
            for (String name : raceColumnNamesToOverride) {
                seriesToOverride.get(0).addRaceColumn(name, destService);
            }

            // Create competitor with same ID and other details
            Set<Competitor> competitorsToOverride = new HashSet<Competitor>();
            Set<DynamicPerson> sailorsToOverride = new HashSet<DynamicPerson>();
            sailorsToOverride.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(
                    645487200000L), "Oberhoschy"));
            Person coachToOverride = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(
                    645487200000L), "Der Lennart halt");
            DynamicTeam teamToOverride = new TeamImpl("Pros", sailorsToOverride, coachToOverride);
            BoatClass boatClassToOverride = new BoatClassImpl("H16", true);
            DynamicBoat boatToOverride = new BoatImpl("Wingy", boatClassToOverride, "GER70133");
            String competitorOldName = "oldName";
            Competitor competitorToOverride = domainFactory.getOrCreateCompetitor(competitorUUID, competitorOldName,
                    Color.BLUE,
                    teamToOverride, boatToOverride);
            competitorsToOverride.add(competitorToOverride);

            Leaderboard leaderboardToOverride = destService.addRegattaLeaderboard(
                    regattaToOverride.getRegattaIdentifier(), "testDisplayNameNotToOverride", discardRule);
            TrackedRace trackedRace2 = new DummyTrackedRace(competitorsToOverride, regattaToOverride, null);
            RaceColumn columnToOverride = leaderboardToOverride.getRaceColumns().iterator().next();
            columnToOverride.setTrackedRace(testFleet1ToOverride, trackedRace2);
            identifierOfRegattaTrackedRace = regattaToOverride
                    .getRaceIdentifier(columnToOverride.getRaceDefinition(testFleet1ToOverride));

            destService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDescToOverride", false, new ArrayList<String>(),
                    null, null);
            destService.getLeaderboardGroupByName(TEST_GROUP_NAME).addLeaderboard(leaderboardToOverride);
            destService.addLeaderboard(leaderboardToOverride);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, true);

        // ---Asserts---
        // Test correct number of creations
        Assert.assertNotNull(creationCount);
        Assert.assertEquals(1, creationCount.getEventCount());
        Assert.assertEquals(1, creationCount.getRegattaCount());
        Assert.assertEquals(1, creationCount.getLeaderboardCount());
        Assert.assertEquals(1, creationCount.getLeaderboardGroupCount());

        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);

        // Check if existing event didn't survive import
        Assert.assertEquals(event.getVenue().getName(), eventOnTarget.getVenue().getName());

        // Check if existing course area survive import
        Assert.assertEquals(courseArea.getName(), eventOnTarget.getVenue().getCourseAreas().iterator().next().getName());
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        // Check if existing leaderboard group didn't survive import
        Assert.assertEquals(group.getDescription(), leaderboardGroupOnTarget.getDescription());
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_REGATTA_NAME + " (" + TEST_BOAT_CLASS_NAME + ")");
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);
        // Check if existing leaderboard didn't survive import
        Assert.assertEquals(leaderboard.getDisplayName(), leaderboardOnTarget.getDisplayName());
        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));

        // Check that tracked race of regatta leaderboard has been removed
        Assert.assertNull(destService.getTrackedRace(identifierOfRegattaTrackedRace));

        // Assert that competitor details were overridden
        Competitor competitorOnTarget = destService.getBaseDomainFactory().getExistingCompetitorById(competitorUUID);
        Assert.assertEquals(competitor.getName(), competitorOnTarget.getName());
        Assert.assertEquals(competitor.getColor(), competitorOnTarget.getColor());

    }

    @Test
    public void testMasterDataImportForRegattaDefaultProcedureAndDesigner() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        RacingEventService sourceService = new RacingEventServiceImpl();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, UUID.randomUUID(),
                new ArrayList<Series>(), true, new LowPoint(), null);
        // Let's use the setters directly because we are not testing replication
        RegattaConfigurationImpl configuration = new RegattaConfigurationImpl();
        configuration.setDefaultCourseDesignerMode(CourseDesignerMode.BY_MAP);
        regatta.setRegattaConfiguration(configuration);

        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", new int[] { 1, 2, 3, 4 });
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        importer.importMasterData(deserializedMasterData, randomUUID, false);

        Regatta importedRegatta = destService.getRegattaByName(regatta.getName());

        assertNotNull(importedRegatta.getRegattaConfiguration());
        assertEquals(CourseDesignerMode.BY_MAP, importedRegatta.getRegattaConfiguration()
                .getDefaultCourseDesignerMode());
    }

    @Test
    public void testMasterDataImportForRegattaWithoutCourseArea() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), null);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }
        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // ---Asserts---
        Assert.assertNotNull(creationCount);

        // Check if existing event survived import
        Assert.assertNotNull(destService.getRegattaByName(regatta.getName()));

    }

    @Test
    public void testMasterDataImportForPersistentRegattaRaceIDsWithoutHttpStack() throws MalformedURLException,
            IOException, InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);
        Set<String> raceIds = new HashSet<String>();
        raceIds.add("dummy");

        sourceService.setPersistentRegattaForRaceIDs(regatta, raceIds, false);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // ---Asserts---

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);

        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        // Check if dummy race id has been imported to destination service
        ConcurrentHashMap<String, Regatta> map = destService.getPersistentRegattasForRaceIDs();
        Assert.assertEquals(regattaOnTarget, map.get("dummy"));

        // Check if persistent regatta for race id has been persisted
        RacingEventServiceImplMock destService2 = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
        ConcurrentHashMap<String, Regatta> map2 = destService2.getPersistentRegattasForRaceIDs();
        Regatta regattaOnTarget2 = destService2.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertEquals(regattaOnTarget2, map2.get("dummy"));

    }

    @Test
    public void testMasterDataImportForMediaTracks() throws MalformedURLException, IOException, InterruptedException,
            ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        MediaTrack trackOnSource = new MediaTrack("testTitle", "http://test/test.mp4", new Date(0), 2000,
                MediaTrack.MimeType.mp4);
        sourceService.mediaTrackAdded(trackOnSource);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // ---Asserts---

        Assert.assertNotNull(creationCount);

        Collection<MediaTrack> targetTracks = destService.getAllMediaTracks();

        Assert.assertEquals(1, targetTracks.size());

        MediaTrack trackOnTarget = targetTracks.iterator().next();

        Assert.assertEquals(trackOnSource.dbId, trackOnTarget.dbId);

        Assert.assertEquals(trackOnSource.url, trackOnTarget.url);

    }

    @Test
    public void testMasterDataImportWithTwoLgsWithSameLeaderboard() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME, regattaUUID, series,
                true, new LowPoint(), courseAreaUUID);
        event.addRegatta(regatta);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group1 = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, null, null);
        LeaderboardGroup group2 = sourceService.addLeaderboardGroup(TEST_GROUP_NAME2, "testGroupDesc2", false,
                leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", Color.RED, team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        DynamicBoat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", Color.RED, team2,
                boat2);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);

        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta, null);

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(group1);
        groupsToExport.add(group2);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // Test correct number of creations
        Assert.assertNotNull(creationCount);
        Assert.assertEquals(1, creationCount.getEventCount());
        Assert.assertEquals(1, creationCount.getRegattaCount());
        Assert.assertEquals(1, creationCount.getLeaderboardCount());
        Assert.assertEquals(2, creationCount.getLeaderboardGroupCount());

        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Assert.assertTrue(leaderboardGroupOnTarget.getLeaderboards().iterator().hasNext());
        LeaderboardGroup leaderboardGroup2OnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME2);
        Assert.assertNotNull(leaderboardGroup2OnTarget);
        Assert.assertTrue(leaderboardGroup2OnTarget.getLeaderboards().iterator().hasNext());

    }

    @Test
    public void testMasterDataImportWithOverallLeaderboard() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();

        int[] discardRule = { 1, 2, 3, 4 };
        ScoringScheme scheme = new LowPoint();
        List<String> leaderboardNames = new ArrayList<String>();
        LeaderboardGroup sourceGroup = sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false,
                leaderboardNames, discardRule, scheme.getType());
        FlexibleLeaderboard sourceLeaderboard1 = new FlexibleLeaderboardImpl("Leaderboard1", null, scheme, null);
        sourceService.addLeaderboard(sourceLeaderboard1);
        sourceGroup.addLeaderboard(sourceLeaderboard1);

        LeaderboardGroupMetaLeaderboard metaLeaderboard = (LeaderboardGroupMetaLeaderboard) sourceGroup
                .getOverallLeaderboard();
        double factor = 2.6;
        metaLeaderboard.getRaceColumns().iterator().next().setFactor(factor);

        // Serialize
        HashSet<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();
        groupsToExport.add(sourceGroup);
        TopLevelMasterData masterdata = new TopLevelMasterData(groupsToExport, sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs(), sourceService.getAllMediaTracks());
        Assert.assertNotNull(masterdata);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        RacingEventService destService;
        ObjectInputStream ois = null;
        TopLevelMasterData deserializedMasterData;
        DomainFactory domainFactory;
        UUID randomUUID = UUID.randomUUID();
        try {
            masterdata.setMasterDataExportFlagOnRaceColumns(true);
            oos.writeObject(masterdata);

            outputStream.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteCreatedDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID));
            domainFactory = destService.getBaseDomainFactory();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream);
            deserializedMasterData = (TopLevelMasterData) ois.readObject();
        } finally {
            masterdata.setMasterDataExportFlagOnRaceColumns(false);
            oos.close();
            ois.close();
        }
        MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
        MasterDataImportObjectCreationCount creationCount = importer.importMasterData(deserializedMasterData,
                randomUUID, false);

        // Test correct number of creations
        Assert.assertEquals(1, creationCount.getLeaderboardGroupCount());

        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        LeaderboardGroupMetaLeaderboard overallLeaderboard = (LeaderboardGroupMetaLeaderboard) leaderboardGroupOnTarget
                .getOverallLeaderboard();
        Assert.assertNotNull(overallLeaderboard);

        Assert.assertNotNull(overallLeaderboard.getResultDiscardingRule());

        Assert.assertNotNull(overallLeaderboard.getScoringScheme());

        Assert.assertEquals(scheme.getType(), overallLeaderboard.getScoringScheme().getType());

        Assert.assertEquals(3, ((ThresholdBasedResultDiscardingRule) overallLeaderboard.getResultDiscardingRule())
                .getDiscardIndexResultsStartingWithHowManyRaces()[2]);

        Iterable<RaceColumn> metaColumns = overallLeaderboard.getRaceColumns();

        RaceColumn metaColumn = metaColumns.iterator().next();
        Assert.assertNotNull(metaColumn);
        Assert.assertEquals(factor, metaColumn.getFactor());

        // Verify that overall leaderboard data has been persisted
        RacingEventService persistenceVerifier = new RacingEventServiceImplMock();
        LeaderboardGroup lg = persistenceVerifier.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(lg);
        overallLeaderboard = (LeaderboardGroupMetaLeaderboard) lg.getOverallLeaderboard();
        Assert.assertNotNull(overallLeaderboard);

        Assert.assertNotNull(overallLeaderboard.getResultDiscardingRule());

        Assert.assertNotNull(overallLeaderboard.getScoringScheme());

        Assert.assertEquals(scheme.getType(), overallLeaderboard.getScoringScheme().getType());

        Assert.assertEquals(3, ((ThresholdBasedResultDiscardingRule) overallLeaderboard.getResultDiscardingRule())
                .getDiscardIndexResultsStartingWithHowManyRaces()[2]);

        metaColumns = overallLeaderboard.getRaceColumns();

        metaColumn = metaColumns.iterator().next();
        Assert.assertNotNull(metaColumn);
        Assert.assertEquals(factor, metaColumn.getFactor());

    }
}
