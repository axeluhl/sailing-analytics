package com.sap.sailing.server.test;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Test;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.sap.sailing.domain.base.Boat;
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
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MasterDataImportObjectCreationCountImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.TopLevelMasterDataSerializer;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.DummyTrackedRace;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;

public class MasterDataImportTest {

    private static final String TEST_GROUP_NAME = "testGroup";
    private static final String TEST_EVENT_NAME = "testEvent";
    private static final String TEST_LEADERBOARD_NAME = "testRegatta (29er)";

    private final UUID eventUUID = UUID.randomUUID();

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
        //Removes all race log events
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
    }

    @Test
    public void testMasterDataImportWithoutHttpStack() throws MalformedURLException, IOException, InterruptedException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID,
                new ArrayList<String>());
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, raceColumnNames, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta("testRegatta", "29er", regattaUUID, series, true, new LowPoint(),
                courseAreaUUID);
        event.addRegatta(regatta);
        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false, leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<Person> sailors = new HashSet<Person>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        Team team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        Boat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<Person> sailors2 = new HashSet<Person>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        Team team2 = new TeamImpl("Noobs", sailors2, coach2);
        Boat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", team2, boat2);
        competitors.add(competitorToSuppress);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta);

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, 1,
                logTimePoint);
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
        TopLevelMasterDataSerializer serializer = new TopLevelMasterDataSerializer(
                sourceService.getLeaderboardGroups(), sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs());
        Set<String> names = new HashSet<String>();
        names.add(TEST_GROUP_NAME);
        JSONArray masterDataOverallArray = serializer.serialize(names);
        Assert.assertNotNull(masterDataOverallArray);

        // Delete all data above from the database, to allow recreating all of it on target server
        deleteCreatedDataFromDatabase();

        // Deserialization copied from doPost in MasterDataByLeaderboardGroupJsonPostServlet
        RacingEventService destService = new RacingEventServiceImplMock();
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = LeaderboardGroupMasterDataJsonDeserializer
                .create(domainFactory);
        JSONArray leaderboardGroupsMasterDataJsonArray = masterDataOverallArray;
        // Actual import. Roughly copied from doPost in MasterDataByLeaderboardGroupJsonPostServlet
        for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
            JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
            LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                    .deserialize(leaderBoardGroupMasterDataJson);
            ImportMasterDataOperation op = new ImportMasterDataOperation(masterData);
            creationCount.add(destService.apply(op));
        }

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
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        TrackedRace trackedRaceForTarget = new DummyTrackedRace(competitorsCreatedOnTarget, regattaOnTarget);
        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
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
        Assert.assertEquals(maxPointsReason,
                leaderboardOnTarget.getScoreCorrection().getMaxPointsReason(competitorOnTarget, raceColumnOnTarget));

        // Check for carried points
        Assert.assertEquals(carriedPoints, leaderboardOnTarget.getCarriedPoints(competitorOnTarget));

        // Check for suppressed competitor
        Assert.assertTrue(leaderboardOnTarget.getSuppressedCompetitors().iterator().hasNext());
        Competitor suppressedCompetitorOnTarget = domainFactory.getExistingCompetitorById(competitorToSuppressUUID);
        Assert.assertEquals(suppressedCompetitorOnTarget, leaderboardOnTarget.getSuppressedCompetitors().iterator()
                .next());

        // Check for competitor desplay name
        Assert.assertEquals(nickName, leaderboardOnTarget.getDisplayName(suppressedCompetitorOnTarget));

        // Check for race log event
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFix());
        Assert.assertEquals(logEvent.getId(), raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFixAtOrAfter(logTimePoint).getId());
    }
    
    @Test
    public void testMasterDataImportWithoutOverrideWithoutHttpStack() throws MalformedURLException, IOException, InterruptedException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID,
                new ArrayList<String>());
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, raceColumnNames, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta("testRegatta", "29er", regattaUUID, series, true, new LowPoint(),
                courseAreaUUID);
        event.addRegatta(regatta);
        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false, leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<Person> sailors = new HashSet<Person>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        Team team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        Boat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<Person> sailors2 = new HashSet<Person>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        Team team2 = new TeamImpl("Noobs", sailors2, coach2);
        Boat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", team2, boat2);
        competitors.add(competitorToSuppress);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta);

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, 1,
                logTimePoint);
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
        TopLevelMasterDataSerializer serializer = new TopLevelMasterDataSerializer(
                sourceService.getLeaderboardGroups(), sourceService.getAllEvents(), sourceService.getPersistentRegattasForRaceIDs());
        Set<String> names = new HashSet<String>();
        names.add(TEST_GROUP_NAME);
        JSONArray masterDataOverallArray = serializer.serialize(names);
        Assert.assertNotNull(masterDataOverallArray);

        // Delete all data above from the database, to allow recreating all of it on target server
        deleteCreatedDataFromDatabase();
        
        // Create existing data on target
        RacingEventService destService = new RacingEventServiceImplMock();
        String venueNameNotToOverride = "doNotOverride";
        Event eventNotToOverride = destService.addEvent(TEST_EVENT_NAME, venueNameNotToOverride, "", false, eventUUID,
                new ArrayList<String>());
        CourseArea courseAreaNotToOverride = new CourseAreaImpl("testAreaNotToOverride", courseAreaUUID);
        eventNotToOverride.getVenue().addCourseArea(courseAreaNotToOverride);
        
        List<String> raceColumnNamesNotToOverride = new ArrayList<String>();
        String raceColumnNameNotToOveride = "T1nottooverride";
        raceColumnNamesNotToOverride.add(raceColumnNameNotToOveride);

        List<Series> seriesNotToOverride = new ArrayList<Series>();
        List<Fleet> fleetsNotToOverride = new ArrayList<Fleet>();
        FleetImpl testFleet1NotToOverride = new FleetImpl("testFleet1");
        fleetsNotToOverride.add(testFleet1NotToOverride);
        seriesNotToOverride.add(new SeriesImpl("testSeries", false, fleetsNotToOverride, raceColumnNamesNotToOverride, destService));
        Regatta regattaNotToOverride = destService.createRegatta("testRegatta", "29er", regattaUUID, seriesNotToOverride, true, new LowPoint(),
                courseAreaUUID);
        event.addRegatta(regattaNotToOverride);
        Leaderboard leaderboardNotToOverride = destService.addRegattaLeaderboard(regattaNotToOverride.getRegattaIdentifier(),
                "testDisplayNameNotToOverride", discardRule);
        List<String> leaderboardNamesNotToOverride = new ArrayList<String>();
        leaderboardNamesNotToOverride.add(leaderboardNotToOverride.getName());
        LeaderboardGroup groupNotToOverride = destService.addLeaderboardGroup(TEST_GROUP_NAME,
                "testGroupDescNotToOverride", false, leaderboardNamesNotToOverride, null, null);

        // Deserialization copied from SailingServiceImpl
        
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = LeaderboardGroupMasterDataJsonDeserializer
                .create(domainFactory);
        JSONArray leaderboardGroupsMasterDataJsonArray = masterDataOverallArray;
        // Actual import. Roughly copied from SailingServiceImpl
        for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
            JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
            LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                    .deserialize(leaderBoardGroupMasterDataJson);
            ImportMasterDataOperation op = new ImportMasterDataOperation(masterData);
            creationCount.add(destService.apply(op));
        }
        
        //---Asserts---
        
        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        
        //Check if existing event survived import
        Assert.assertEquals(venueNameNotToOverride, eventOnTarget.getVenue().getName());
        
        //Check if existing course area survived import
        Assert.assertEquals(courseAreaNotToOverride.getName(), eventOnTarget.getVenue().getCourseAreas().iterator().next().getName());
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        //Check if existing leaderboard group survived import
        Assert.assertEquals(groupNotToOverride.getDescription(), leaderboardGroupOnTarget.getDescription());
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnNameNotToOveride);
        Assert.assertNotNull(raceColumnOnTarget);
        //Check if existing leaderboard survived import
        Assert.assertEquals(leaderboardNotToOverride.getDisplayName(), leaderboardOnTarget.getDisplayName());
        Assert.assertFalse(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        
    }
    
    @Test
    public void testMasterDataImportForPersistentRegattaRaceIDsWithoutHttpStack() throws MalformedURLException,
            IOException, InterruptedException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, "testVenue", "", false, eventUUID,
                new ArrayList<String>());
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, fleets, raceColumnNames, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta("testRegatta", "29er", regattaUUID, series, true, new LowPoint(),
                courseAreaUUID);
        event.addRegatta(regatta);
        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        sourceService.addLeaderboardGroup(TEST_GROUP_NAME, "testGroupDesc", false, leaderboardNames, null, null);

        // Set tracked Race with competitors
        Set<Competitor> competitors = new HashSet<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<Person> sailors = new HashSet<Person>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        Person coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        Team team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        Boat boat = new BoatImpl("Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", team, boat);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<Person> sailors2 = new HashSet<Person>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        Person coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        Team team2 = new TeamImpl("Noobs", sailors2, coach2);
        Boat boat2 = new BoatImpl("LahmeEnte", boatClass, "GER1337");
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", team2, boat2);
        competitors.add(competitorToSuppress);
        TrackedRace trackedRace = new DummyTrackedRace(competitors, regatta);

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        raceColumn.setTrackedRace(testFleet1, trackedRace);
        Set<String> raceIds = new HashSet<String>();
        raceIds.add("dummy");
        
        sourceService.setPersistentRegattaForRaceIDs(regatta, raceIds, false);

        // Set log event
        RaceLogEventFactory factory = new RaceLogEventFactoryImpl();
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = factory.createStartTimeEvent(logTimePoint, 1, logTimePoint);
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
        TopLevelMasterDataSerializer serializer = new TopLevelMasterDataSerializer(
                sourceService.getLeaderboardGroups(), sourceService.getAllEvents(),
                sourceService.getPersistentRegattasForRaceIDs());
        Set<String> names = new HashSet<String>();
        names.add(TEST_GROUP_NAME);
        JSONArray masterDataOverallArray = serializer.serialize(names);
        Assert.assertNotNull(masterDataOverallArray);

        // Delete all data above from the database, to allow recreating all of it on target server
        deleteCreatedDataFromDatabase();

        // Deserialization copied from SailingServiceImpl

        RacingEventService destService = new RacingEventServiceImplMock();
        DomainFactory domainFactory = DomainFactory.INSTANCE;
        MasterDataImportObjectCreationCountImpl creationCount = new MasterDataImportObjectCreationCountImpl();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = LeaderboardGroupMasterDataJsonDeserializer
                .create(domainFactory);
        JSONArray leaderboardGroupsMasterDataJsonArray = masterDataOverallArray;
        // Actual import. Roughly copied from SailingServiceImpl
        for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
            JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
            LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                    .deserialize(leaderBoardGroupMasterDataJson);
            ImportMasterDataOperation op = new ImportMasterDataOperation(masterData);
            creationCount.add(destService.apply(op));
        }

        // ---Asserts---

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);

        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);
        
        //Check if dummy race id has been imported to destination service
        ConcurrentHashMap<String, Regatta> map = destService.getPersistentRegattasForRaceIDs();
        Assert.assertEquals(regattaOnTarget, map.get("dummy"));

    }
}
