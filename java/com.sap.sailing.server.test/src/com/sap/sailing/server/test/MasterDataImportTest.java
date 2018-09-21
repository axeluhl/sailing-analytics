package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.WriteConcern;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningConfirmedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
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
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.racelog.tracking.EmptySensorFixStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.TrackBasedTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.spi.MasterDataResource;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.masterdata.DummyTrackedRace;
import com.sap.sailing.server.masterdata.MasterDataImporter;
import com.sap.sailing.server.testsupport.RacingEventServiceImplMock;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;

import junit.framework.Assert;

public class MasterDataImportTest {

    private static final String TEST_GROUP_NAME = "testGroup";
    private static final String TEST_EVENT_NAME = "testEvent";
    private static final String TEST_REGATTA_NAME = "testRegatta";
    private static final String TEST_BOAT_CLASS_NAME = "29er";
    private static final String TEST_LEADERBOARD_NAME = "testRegatta (29er)";
    private static final String TEST_GROUP_NAME2 = "testGroup2";

    private final UUID eventUUID = UUID.randomUUID();
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    private final TimePoint eventStartDate = new MillisecondsTimePoint(new Date());
    private final TimePoint eventEndDate = new MillisecondsTimePoint(new Date());

    private final UUID raceId = UUID.randomUUID();

    /**
     * Log Events created when running test. Will be removed from db at teardown
     */
    private Set<Serializable> storedLogUUIDs = new HashSet<Serializable>();

    @After
    public void tearDown() {
        deleteAllDataFromDatabase();
    }

    @Before
    public void setUp() {
        deleteAllDataFromDatabase();
    }

    private <T extends AbstractSailingServerResource> T spyResource(T resource, RacingEventService service) {
        T spyResource = spy(resource);

        doReturn(service).when(spyResource).getService();
        return spyResource;
    }

    private void deleteAllDataFromDatabase() {
        MongoDBService service = MongoDBConfiguration.getDefaultTestConfiguration().getService();
        service.getDB().getWriteConcern().fsync();
        service.getDB().dropDatabase();
    }

    private Map<Competitor, Boat> createCompetitorsAndBoatsMap(BoatClass boatClass, Iterable<Competitor> competitors) {
        Map<Competitor, Boat> result = new LinkedHashMap<>();
        int i = 1;
        for(Competitor c: competitors) {
            Boat b = new BoatImpl(c.getId(), "Boat of " + c.getName(), boatClass, "GER " + i++, null);
            result.put(c, b);
        }
        return result;
    }

    @Test
    public void testMasterDataImportWithoutHttpStack() throws MalformedURLException, IOException, InterruptedException,
            ClassNotFoundException {
        // Setup source service
        MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
        RacingEventService sourceService = new RacingEventServiceImpl(null, null, serviceFinderFactory);
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED,
                /* startDate */null, /* endDate */null, regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

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
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);
        event.addLeaderboardGroup(group);

        // Set tracked Race with competitors
        List<Competitor> competitors = new ArrayList<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        DynamicPerson coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        TimePoint logTimePoint2 = new MillisecondsTimePoint(1372489201000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, logTimePoint, author, 1,
                0, logTimePoint, RaceLogRaceStatus.SCHEDULED);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        Position p = new DegreePosition(3, 3);
        Wind wind = new WindImpl(p, logTimePoint2, new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(12)));
        RaceLogWindFixEvent windEvent = new RaceLogWindFixEventImpl(logTimePoint2, logTimePoint2, author,
                UUID.randomUUID(), 2, wind, /* isMagnetic */false);
        raceColumn.getRaceLog(testFleet1).add(windEvent);
        storedLogUUIDs.add(logEvent.getId());
        storedLogUUIDs.add(windEvent.getId());

        // Set RegattaLog event
        TimePoint regattaLogTimepoint = new MillisecondsTimePoint(84392048L);
        RegattaLogRegisterCompetitorEvent registerEvent = new RegattaLogRegisterCompetitorEventImpl(
                regattaLogTimepoint, regattaLogTimepoint, author, UUID.randomUUID(), competitor);
        regatta.getRegattaLog().add(registerEvent);

        // Add some racelogtracking stuff
        TimePoint logTimePoint3 = new MillisecondsTimePoint(1372489210000L);
        TimePoint logTimePoint4 = new MillisecondsTimePoint(1372489200020L);
        DeviceIdentifier deviceIdentifier = new SmartphoneImeiIdentifier("a");
        RegattaLogDeviceCompetitorMappingEvent mappingEvent = new RegattaLogDeviceCompetitorMappingEventImpl(
                logTimePoint4, logTimePoint4, author, UUID.randomUUID(), competitor, deviceIdentifier, logTimePoint,
                logTimePoint3);
        regatta.getRegattaLog().add(mappingEvent);
        GPSFix gpsFix = new GPSFixMovingImpl(new DegreePosition(54.333, 10.133), logTimePoint2,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(90)));
        sourceService.getSensorFixStore().storeFix(deviceIdentifier, gpsFix);
        
        // test to check that batch-import of fixes works as intended
        DeviceIdentifier deviceBatch1 = addDeviceMappingWithFixes(sourceService, regatta, competitor, logTimePoint, logTimePoint3, logTimePoint4, "x", 4999);
        DeviceIdentifier deviceBatch2 = addDeviceMappingWithFixes(sourceService, regatta, competitor, logTimePoint, logTimePoint3, logTimePoint4, "y", 5000);
        DeviceIdentifier deviceBatch3 = addDeviceMappingWithFixes(sourceService, regatta, competitor, logTimePoint, logTimePoint3, logTimePoint4, "z", 5001);
        
        // Add sensor fix
        DeviceIdentifier deviceIdentifier2 = new SmartphoneImeiIdentifier("b");
        RegattaLogDeviceMappingEvent<Competitor> bravoMappingEvent = new RegattaLogDeviceCompetitorBravoMappingEventImpl(
                logTimePoint4, author, competitor, deviceIdentifier2, logTimePoint,
                logTimePoint3);
        regatta.getRegattaLog().add(bravoMappingEvent);
        Double[] fixData = new Double[BravoSensorDataMetadata.getTrackColumnCount()];
        double rideHeightValue = 1337.0;
        fixData[BravoSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex()] = rideHeightValue;
        fixData[BravoSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex()] = rideHeightValue;
        DoubleVectorFixImpl doubleVectorFix = new DoubleVectorFixImpl(logTimePoint2, fixData);
        sourceService.getSensorFixStore().storeFix(deviceIdentifier2, doubleVectorFix);

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
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        InputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID), serviceFinderFactory){};
            domainFactory = destService.getBaseDomainFactory();
            DB db = destService.getMongoObjectFactory().getDatabase();
            db.setWriteConcern(WriteConcern.SAFE);
            inputStream = new ByteArrayInputStream(os.toByteArray());
            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        assertSame(leaderboardGroupOnTarget, eventOnTarget.getLeaderboardGroups().iterator().next());
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(leaderboardOnTarget);
        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        Assert.assertEquals(false, regattaOnTarget.getAllRaces().iterator().hasNext());

        Assert.assertEquals(courseAreaUUID, eventOnTarget.getVenue().getCourseAreas().iterator().next().getId());

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumnByName(raceColumnName);
        Assert.assertNotNull(raceColumnOnTarget);
        Assert.assertNull(raceColumnOnTarget.getTrackedRace(raceColumnOnTarget.getFleetByName(testFleet1Name)));

        raceColumnOnTarget.setTrackedRace(raceColumnOnTarget.getFleets().iterator().next(), new DummyTrackedRace(
                raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regattaOnTarget, null, sourceService.getWindStore()));

        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        Competitor competitorOnTarget = domainFactory.getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitorsCreatedOnTarget), regattaOnTarget,
                null, sourceService.getWindStore());

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        Assert.assertEquals(factor, leaderboard.getScoringScheme().getScoreFactor(raceColumnOnTarget));

        Iterable<Competitor> competitorsOnTarget = leaderboardOnTarget.getAllCompetitors();
        Iterator<Competitor> competitorIterator = competitorsOnTarget.iterator();
        Assert.assertTrue(competitorIterator.hasNext());
        Assert.assertEquals(competitorOnTarget, competitorIterator.next());

        // Check for score corrections
        Assert.assertEquals(
                scoreCorrection,
                leaderboardOnTarget.getScoreCorrection().getExplicitScoreCorrection(competitorOnTarget,
                        raceColumnOnTarget));
        Assert.assertEquals(maxPointsReason, leaderboardOnTarget.getScoreCorrection()
                .getMaxPointsReason(competitorOnTarget, raceColumnOnTarget, MillisecondsTimePoint.now()));

        // Check for carried points
        Assert.assertEquals(carriedPoints, leaderboardOnTarget.getCarriedPoints(competitorOnTarget));

        // Check for suppressed competitor
        Assert.assertTrue(leaderboardOnTarget.getSuppressedCompetitors().iterator().hasNext());
        Competitor suppressedCompetitorOnTarget = domainFactory.getCompetitorAndBoatStore().getExistingCompetitorById(
                competitorToSuppressUUID);
        Assert.assertEquals(suppressedCompetitorOnTarget, leaderboardOnTarget.getSuppressedCompetitors().iterator()
                .next());

        // Check for competitor display name
        Assert.assertEquals(nickName, leaderboardOnTarget.getDisplayName(suppressedCompetitorOnTarget));

        // Check for race log event
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFixAtOrAfter(logTimePoint));
        Assert.assertEquals(logEvent.getId(),
                raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstRawFixAtOrAfter(logTimePoint).getId());
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(fleet1OnTarget).getFirstFixAtOrAfter(logTimePoint2));
        Assert.assertEquals(wind, ((RaceLogWindFixEvent) raceColumnOnTarget.getRaceLog(fleet1OnTarget)
                .getFirstFixAtOrAfter(logTimePoint2)).getWindFix());

        // Add new event to check persistence of post-import events (see bug 3230)
        TimePoint logTimePoint5 = new MillisecondsTimePoint(1372489310000L);
        UUID postImportEventId = UUID.randomUUID();
        RaceLogStartTimeEventImpl postImportLogEvent = new RaceLogStartTimeEventImpl(logTimePoint5, logTimePoint5,
                author, postImportEventId, 3, logTimePoint5, RaceLogRaceStatus.SCHEDULED);
        raceColumnOnTarget.getRaceLog(fleet1OnTarget).add(postImportLogEvent);

        // Check for regatta log event
        RegattaLogRegisterCompetitorEvent registerEventOnTarget = (RegattaLogRegisterCompetitorEvent) regattaOnTarget
                .getRegattaLog().getFirstFixAtOrAfter(regattaLogTimepoint);
        Assert.assertNotNull(registerEventOnTarget);
        Assert.assertEquals(registerEvent.getId(), registerEventOnTarget.getId());
        Assert.assertEquals(competitor.getId(), registerEventOnTarget.getCompetitor().getId());

        // Check for import of racelogtracking fix
        SensorFixStore sensorFixStore = destService.getSensorFixStore();
        Assert.assertEquals(1, sensorFixStore.getNumberOfFixes(deviceIdentifier));
        verifyFix(gpsFix, sensorFixStore, logTimePoint, logTimePoint3, deviceIdentifier);
        
        // Check that fix counts around the batch size are imported
        verifyFixCount(sensorFixStore, deviceBatch1, 4999);
        verifyFixCount(sensorFixStore, deviceBatch2, 5000);
        verifyFixCount(sensorFixStore, deviceBatch3, 5001);
        
        // Check for import of sensor fix
        Assert.assertEquals(1, sensorFixStore.getNumberOfFixes(deviceIdentifier2));
        verifyFix(doubleVectorFix, sensorFixStore, logTimePoint, logTimePoint3, deviceIdentifier2);

        // Check for persisting of race log events:
        RacingEventService dest2 = new RacingEventServiceImplMock(){};
        Leaderboard lb2 = dest2.getLeaderboardByName(TEST_LEADERBOARD_NAME);
        RaceColumn raceColumn2 = lb2.getRaceColumns().iterator().next();
        RaceLog raceLog2 = raceColumn2.getRaceLog(raceColumn2.getFleetByName(fleet1OnTarget.getName()));
        Assert.assertEquals(logEvent.getId(), raceLog2.getFirstRawFixAtOrAfter(logTimePoint).getId());

        RaceLogEvent postImportLogEventFromDB = raceLog2.getFirstRawFixAtOrAfter(logTimePoint5);
        Assert.assertEquals(postImportEventId, postImportLogEventFromDB.getId());

        // Check for persisting of regatta log events
        Regatta regattaOnTarget2 = dest2.getRegattaByName(TEST_LEADERBOARD_NAME);
        RegattaLogRegisterCompetitorEvent registerEventOnTarget2 = (RegattaLogRegisterCompetitorEvent) regattaOnTarget2
                .getRegattaLog().getFirstFixAtOrAfter(regattaLogTimepoint);
        Assert.assertNotNull(registerEventOnTarget2);
        Assert.assertEquals(registerEvent.getId(), registerEventOnTarget2.getId());
    }

    private DeviceIdentifier addDeviceMappingWithFixes(RacingEventService sourceService, Regatta regatta, CompetitorImpl competitor,
            TimePoint logTimePoint, TimePoint logTimePoint3, TimePoint logTimePoint4, String imei, int numFixes) {
        DeviceIdentifier deviceIdentifier = new SmartphoneImeiIdentifier(imei);
        RegattaLogDeviceCompetitorMappingEvent mappingEvent = new RegattaLogDeviceCompetitorMappingEventImpl(
                logTimePoint4, logTimePoint4, author, UUID.randomUUID(), competitor, deviceIdentifier, logTimePoint,
                logTimePoint3);
        regatta.getRegattaLog().add(mappingEvent);
        List<GPSFix> fixesToSave = new ArrayList<>(numFixes);
        for(int i = 1; i <= numFixes; i++) {
            fixesToSave.add(new GPSFixMovingImpl(new DegreePosition(54.333, 10.133), logTimePoint.plus(i),
                    new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(90))));
        }
        sourceService.getSensorFixStore().storeFixes(deviceIdentifier, fixesToSave);
        return deviceIdentifier;
    }
    
    private void verifyFix(Timed expectedFix, SensorFixStore store, TimePoint start, TimePoint endInclusive, DeviceIdentifier deviceIdentifier) {
        List<Timed> fixes = new ArrayList<>(1);
        try {
            store.loadFixes(fixes::add, deviceIdentifier, start, endInclusive, /* toIsInclusive */ true);
            assertEquals(1, fixes.size());
            assertEquals(expectedFix, fixes.get(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void verifyFixCount(SensorFixStore store, DeviceIdentifier deviceIdentifier, int numFixes) {
        try {
            assertEquals(numFixes, store.getNumberOfFixes(deviceIdentifier));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMasterDataImportForScoreCorrections() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitor2UUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Test Mustermann", new NationalityImpl("GER"), new Date(645487200000L), "desc"));
        DynamicPerson coach2 = new PersonImpl("Max Test", new NationalityImpl("GER"), new Date(645487200000L), "desc");
        DynamicTeam team2 = new TeamImpl("Pros2", sailors2, coach2);
        CompetitorImpl competitor2 = new CompetitorImpl(competitor2UUID, "Froderik", "KYC", Color.RED, null, null, team2,
                /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor2);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        Competitor competitorOnTarget2 = domainFactory.getCompetitorAndBoatStore().getExistingCompetitorById(competitor2UUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitorsCreatedOnTarget), regattaOnTarget,
                null, sourceService.getWindStore());

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
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(RegattaImpl.getDefaultName("testRegatta", "29er"), "29er",
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

        // Set tracked Race with competitors
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        DynamicPerson coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        DynamicBoat boat = new BoatImpl("123", "Wingy", boatClass, "GER70133");
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        competitorsAndBoats.put(competitor, boat);

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        DynamicTrackedRegatta trackedRegatta = sourceService.getOrCreateTrackedRegatta(regatta);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, competitorsAndBoats, regatta, trackedRegatta,
                sourceService.getWindStore());
        regatta.addRace(trackedRace.getRace()); // make sure the competitor is serialized together with the regatta
        raceColumn.setTrackedRace(testFleet1, trackedRace);
        WindSource windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, "Igtimi1");
        WindTrack windTrackOnSource = trackedRace.getOrCreateWindTrack(windSource);

        Position p = new DegreePosition(54, 30);
        TimePoint at = new MillisecondsTimePoint(20000);
        SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(20, new DegreeBearingImpl(12));
        windTrackOnSource.add(new WindImpl(p, at, windSpeedWithBearing));

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        Boat boatOnTarget = domainFactory.getExistingBoatById(competitorUUID);
        Map<Competitor, Boat> competitorsAndBoatsOnTarget = new HashMap<>();
        competitorsAndBoatsOnTarget.put(competitorOnTarget, boatOnTarget);
        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        DynamicTrackedRegatta trackedRegattaForTarget = destService.getOrCreateTrackedRegatta(regattaOnTarget);
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(raceId, competitorsAndBoatsOnTarget, regattaOnTarget,
                trackedRegattaForTarget, sourceService.getWindStore());

        raceColumnOnTarget.setTrackedRace(fleet1OnTarget, trackedRaceForTarget);

        WindTrack windTrackOnDest = trackedRaceForTarget.getOrCreateWindTrack(windSource);
        windTrackOnDest.lockForRead();
        try {
            Iterable<Wind> fixes = windTrackOnDest.getFixes();
            Assert.assertNotNull(fixes);
            Iterator<Wind> iterator = fixes.iterator();
            Assert.assertTrue(iterator.hasNext());
            Wind firstWindFix = iterator.next();
            Assert.assertNotNull(firstWindFix);

            Assert.assertFalse(iterator.hasNext());
        } finally {
            windTrackOnDest.unlockAfterRead();
        }

        // Reimport again to see if db throws exceptions (see console for error for now, since exception is caught deep
        // inside)
        try {
            streamingOutput.write(os);
            os.flush();

            inputStream = new ByteArrayInputStream(os.toByteArray());
            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

    }

    @Test
    public void testMasterDataImportForRaceLogEventsReferencingCompetitors() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        DomainFactory sourceDomainFactory = new DomainFactoryImpl((srlid) -> null);
        RacingEventService sourceService = new RacingEventServiceImpl(
                PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, sourceDomainFactory),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(MongoDBService.INSTANCE),
                MediaDBFactory.INSTANCE.getDefaultMediaDB(), EmptyWindStore.INSTANCE, EmptySensorFixStore.INSTANCE, /* restoreTrackedRaces */ false);
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null, 
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        Competitor competitor = sourceDomainFactory.getOrCreateCompetitor(competitorUUID, "Froderik", "F", Color.RED,
                "noone@nowhere.de", null, team, /* timeOnTimeFactor */null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */
                null, null);
        competitors.add(competitor);
        UUID competitor2UUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Test Mustermann", new NationalityImpl("GER"), new Date(645487200000L), "desc"));
        DynamicPerson coach2 = new PersonImpl("Max Test", new NationalityImpl("GER"), new Date(645487200000L), "desc");
        DynamicTeam team2 = new TeamImpl("Pros2", sailors2, coach2);
        Competitor competitor2 = sourceDomainFactory.getCompetitorAndBoatStore().getOrCreateCompetitor(competitor2UUID,
                "Froderik", "F", Color.RED, "noone@nowhere.de", null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */
                null, null);
        competitors.add(competitor2);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
        raceColumn.getRaceLog(testFleet1).add(logEvent);
        storedLogUUIDs.add(logEvent.getId());

        // Add a competitor-related race log event to ensure that no competitor resolution is attempted while receiving
        TimePoint logTimePoint2 = logTimePoint.plus(10);
        CompetitorResults positionedCompetitors = new CompetitorResultsImpl();
        positionedCompetitors.add(new CompetitorResultImpl(
                competitor.getId(), competitor.getName(), /* rank */ 1, MaxPointsReason.DNS, /* score */ null, /* finishingTime */ null, /* comment */ null, MergeState.OK));
        positionedCompetitors.add(new CompetitorResultImpl(
                competitor2.getId(), competitor2.getName(), /* rank */ 2, MaxPointsReason.NONE, /* score */ null, /* finishingTime */ null, /* comment */ null, MergeState.OK));
        RaceLogFinishPositioningConfirmedEvent finishPositioningConfirmedEvent = new RaceLogFinishPositioningConfirmedEventImpl(
                logTimePoint2, author, 1, positionedCompetitors);
        raceColumn.getRaceLog(testFleet1).add(finishPositioningConfirmedEvent);
        storedLogUUIDs.add(finishPositioningConfirmedEvent.getId());

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        Competitor competitorOnTarget = domainFactory.getCompetitorAndBoatStore().getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Fleet fleet1OnTarget = raceColumnOnTarget.getFleetByName(testFleet1.getName());
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitorsCreatedOnTarget), regattaOnTarget,
                null, sourceService.getWindStore());

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
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
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
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        String venueNameNotToOverride;
        CourseAreaImpl courseAreaNotToOverride;
        String raceColumnNameNotToOveride;
        RegattaLeaderboard leaderboardNotToOverride;
        LeaderboardGroup groupNotToOverride;
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            // Create existing data on target
            venueNameNotToOverride = "doNotOverride";
            Event eventNotToOverride = destService.addEvent(TEST_EVENT_NAME, /* eventDescription */null,
                    eventStartDate, eventEndDate, venueNameNotToOverride, false, eventUUID);
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
            seriesNotToOverride.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleetsNotToOverride, emptyRaceColumnNamesList,
                    destService));
            Regatta regattaNotToOverride = destService.createRegatta(
                    RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                    /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */ null, /* endDate */null,
                    regattaUUID, seriesNotToOverride, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                    /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

            for (String name : raceColumnNamesNotToOverride) {
                seriesNotToOverride.get(0).addRaceColumn(name, destService);
            }

            leaderboardNotToOverride = destService.addRegattaLeaderboard(regattaNotToOverride.getRegattaIdentifier(),
                    "testDisplayNameNotToOverride", discardRule);
            List<String> leaderboardNamesNotToOverride = new ArrayList<String>();
            leaderboardNamesNotToOverride.add(leaderboardNotToOverride.getName());
            groupNotToOverride = destService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME,
                    "testGroupDescNotToOverride", /* displayName */null, false, leaderboardNamesNotToOverride, null,
                    null);
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
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
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        RegattaAndRaceIdentifier identifierOfRegattaTrackedRace;
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            // Create existing data on target
            String venueNameToOverride = "Override";
            Event eventToOverride = destService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate,
                    eventEndDate, venueNameToOverride, false, eventUUID);
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
            seriesToOverride.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleetsToOverride, emptyRaceColumnNamesList,
                    destService));
            Regatta regattaToOverride = destService.createRegatta(
                    RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME, 
                    /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */ null, /* endDate */null, 
                    regattaUUID, seriesToOverride, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                    /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

            for (String name : raceColumnNamesToOverride) {
                seriesToOverride.get(0).addRaceColumn(name, destService);
            }

            // Create competitor with same ID and other details
            Set<Competitor> competitorsToOverride = new HashSet<Competitor>();
            Set<DynamicPerson> sailorsToOverride = new HashSet<DynamicPerson>();
            sailorsToOverride.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(
                    645487200000L), "Oberhoschy"));
            DynamicPerson coachToOverride = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(
                    645487200000L), "Der Lennart halt");
            DynamicTeam teamToOverride = new TeamImpl("Pros", sailorsToOverride, coachToOverride);
            BoatClass boatClassToOverride = new BoatClassImpl("H16", true);
            String competitorOldName = "oldName";
            Competitor competitorToOverride = domainFactory.getOrCreateCompetitor(competitorUUID, competitorOldName, "c",
                    Color.BLUE, "noone@nowhere.de", null, teamToOverride, /* timeOnTimeFactor */null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */
                    null, null);
            competitorsToOverride.add(competitorToOverride);

            Leaderboard leaderboardToOverride = destService.addRegattaLeaderboard(
                    regattaToOverride.getRegattaIdentifier(), "testDisplayNameNotToOverride", discardRule);
            TrackedRace trackedRace2 = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClassToOverride, competitorsToOverride),
                    regattaToOverride, null, sourceService.getWindStore());
            RaceColumn columnToOverride = leaderboardToOverride.getRaceColumns().iterator().next();
            columnToOverride.setTrackedRace(testFleet1ToOverride, trackedRace2);
            identifierOfRegattaTrackedRace = regattaToOverride.getRaceIdentifier(columnToOverride
                    .getRaceDefinition(testFleet1ToOverride));

            destService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDescToOverride", /* displayName */
                    null, false, new ArrayList<String>(), null, null);
            destService.getLeaderboardGroupByName(TEST_GROUP_NAME).addLeaderboard(leaderboardToOverride);
            destService.addLeaderboard(leaderboardToOverride);
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, true);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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

        RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboardOnTarget;
        Regatta regattaInLeaderboard = regattaLeaderboard.getRegatta();
        Assert.assertSame(regattaOnTarget, regattaInLeaderboard);

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

        Event event = sourceService.createEventWithoutReplication("Test Event", /* eventDescription */null,
                new MillisecondsTimePoint(0), new MillisecondsTimePoint(10), "testvenue", false, UUID.randomUUID(),
                /* officialWebsiteURL */null, /*baseURL*/null,
                /* sailorsInfoWebsiteURL */null, /* videos */
                /* images */Collections.<ImageDescriptor> emptyList(), Collections.<VideoDescriptor> emptyList());
        CourseArea defaultCourseArea = sourceService.addCourseAreas(event.getId(), new String[] { "ECHO" },
                new UUID[] { UUID.randomUUID() })[0];

        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME, 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */
                null, /* endDate */null, UUID.randomUUID(), new ArrayList<Series>(), true, new LowPoint(),
                defaultCourseArea.getId(), /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        // Let's use the setters directly because we are not testing replication
        RegattaConfigurationImpl configuration = new RegattaConfigurationImpl();
        configuration.setDefaultCourseDesignerMode(CourseDesignerMode.BY_MAP);
        regatta.setRegattaConfiguration(configuration);

        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", new int[] { 1, 2, 3, 4 });
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        destService.getDataImportLock().getProgress(randomUUID).getResult();
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

        Event event = sourceService.createEventWithoutReplication("Test Event", /* eventDescription */null,
                new MillisecondsTimePoint(0), new MillisecondsTimePoint(10), "testvenue", false, UUID.randomUUID(),
                /* officialWebsiteURL */null, /*baseURL*/null,
                /* sailorsInfoWebsiteURL */null, /* videos */
                /* images */Collections.<ImageDescriptor> emptyList(), Collections.<VideoDescriptor> emptyList());
        CourseArea defaultCourseArea = sourceService.addCourseAreas(event.getId(), new String[] { "ECHO" },
                new UUID[] { UUID.randomUUID() })[0];

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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();

        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null, 
                regattaUUID, series, true, new LowPoint(),
                defaultCourseArea.getId(), /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */true,
                /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }
        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
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
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
        UUID courseAreaUUID = UUID.randomUUID();
        CourseArea courseArea = new CourseAreaImpl("testArea", courseAreaUUID);
        event.getVenue().addCourseArea(courseArea);

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        String raceColumnName2 = "T2";
        raceColumnNames.add(raceColumnName2);
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        FleetImpl testFleet1 = new FleetImpl("testFleet1");
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);
        sourceService.setRegattaForRace(regatta, "dummy");
        sourceService.setRegattaForRace(regatta, "dummy2");

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
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
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

        // ---Asserts---

        Assert.assertNotNull(creationCount);
        Event eventOnTarget = destService.getEvent(eventUUID);
        Assert.assertNotNull(eventOnTarget);

        Regatta regattaOnTarget = destService.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertNotNull(regattaOnTarget);

        // Check if dummy race id has been imported to destination service
        ConcurrentHashMap<String, Regatta> map = destService.getPersistentRegattasForRaceIDs();
        Assert.assertEquals(regattaOnTarget, map.get("dummy"));
        Assert.assertEquals(regattaOnTarget, map.get("dummy2"));

        // Check if persistent regatta for race id has been persisted
        RacingEventServiceImplMock destService2 = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
        ConcurrentHashMap<String, Regatta> map2 = destService2.getPersistentRegattasForRaceIDs();
        Regatta regattaOnTarget2 = destService2.getRegattaByName(TEST_LEADERBOARD_NAME);
        Assert.assertEquals(regattaOnTarget2, map2.get("dummy"));

    }

    @Test
    public void testMasterDataImportForMediaTracks() throws MalformedURLException, IOException, InterruptedException,
            ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        String regattaName1 = "49er";
        String regattaName2 = "49er FX";
        String missingRegattaName = "Missing Regatta";
        String raceName1 = "R1";
        String raceName2 = "R2";
        String raceName3 = "R3";
        String raceName4 = "R4";
        String raceName5 = "R5";
        String missingRaceName = "Missing Race";
        assignedRaces.add(new RegattaNameAndRaceName(regattaName1, raceName1));
        assignedRaces.add(new RegattaNameAndRaceName(regattaName1, raceName2));
        assignedRaces.add(new RegattaNameAndRaceName(regattaName1, raceName3));
        assignedRaces.add(new RegattaNameAndRaceName(regattaName2, raceName4));
        assignedRaces.add(new RegattaNameAndRaceName(regattaName2, raceName5));
        assignedRaces.add(new RegattaNameAndRaceName(missingRegattaName, missingRaceName));
        MediaTrack trackOnSource = new MediaTrack("testTitle", "http://test/test.mp4", new MillisecondsTimePoint(0),
                MillisecondsDurationImpl.ONE_HOUR, MimeType.mp4, assignedRaces);
        sourceService.mediaTrackAdded(trackOnSource);

        Collection<String> raceColumnNames = Arrays.asList(raceName1, raceName2, raceName3, raceName4, raceName5);
        Regatta regatta = TrackBasedTest.createTestRegatta(regattaName1, raceColumnNames);
        sourceService.addRegattaWithoutReplication(regatta);
        int[] discardThresholds = new int[0];
        RegattaLeaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "leaderboard display name", discardThresholds);
        Collection<RaceIdentifier> raceIdentifiers = Arrays.asList(new RegattaNameAndRaceName(regattaName1, raceName1),
                new RegattaNameAndRaceName(regattaName1, raceName2),
                new RegattaNameAndRaceName(regattaName1, raceName3),
                new RegattaNameAndRaceName(regattaName2, raceName4),
                new RegattaNameAndRaceName(regattaName2, raceName5));
        TrackBasedTest.assignRacesToRegattaLeaderboardColumns(leaderboard, raceIdentifiers);
        boolean displayGroupsInReverseOrder = false;
        int[] overallLeaderboardDiscardThresholds = new int[0];
        UUID leaderboardGropuUuid = UUID.randomUUID();
        LeaderboardGroup leaderboardGroup = sourceService.addLeaderboardGroup(leaderboardGropuUuid,
                "leaderboard group name", "leaderboard group description", "leaderboard group display name",
                displayGroupsInReverseOrder, Collections.singletonList(leaderboard.getName()),
                overallLeaderboardDiscardThresholds, ScoringSchemeType.LOW_POINT);

        // Serialize
        List<String> groupNamesToExport = Collections.singletonList(leaderboardGroup.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }
        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID).getResult();

        // ---Asserts---

        Assert.assertNotNull(creationCount);

        Iterable<MediaTrack> targetTracks = destService.getAllMediaTracks();

        Assert.assertEquals(1, Util.size(targetTracks));

        MediaTrack trackOnTarget = targetTracks.iterator().next();

        Assert.assertEquals(trackOnSource.dbId, trackOnTarget.dbId);

        Assert.assertEquals(trackOnSource.url, trackOnTarget.url);

        Assert.assertEquals(trackOnSource.assignedRaces, trackOnTarget.assignedRaces);

    }

    @Test
    public void testMasterDataImportWithTwoLgsWithSameLeaderboard() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        Event event = sourceService.addEvent(TEST_EVENT_NAME, /* eventDescription */null, eventStartDate, eventEndDate,
                "testVenue", false, eventUUID);
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
        series.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED,
                /* startDate */null, /* endDate */null, regattaUUID, series, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0, 
                /* useStartTimeInference */true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

        for (String name : raceColumnNames) {
            series.get(0).addRaceColumn(name, sourceService);
        }

        int[] discardRule = { 1, 2, 3, 4 };
        Leaderboard leaderboard = sourceService.addRegattaLeaderboard(regatta.getRegattaIdentifier(),
                "testDisplayName", discardRule);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group1 = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME,
                "testGroupDesc",
                /* displayName */null, false, leaderboardNames, null, null);
        LeaderboardGroup group2 = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME2,
                "testGroupDesc2",
                /* displayName */null, false, leaderboardNames, null, null);

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
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);

        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        raceColumn.setTrackedRace(testFleet1, trackedRace);

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group1.getName());
        groupNamesToExport.add(group2.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

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
        LeaderboardGroup sourceGroup = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME,
                "testGroupDesc",
                /* displayName */null, false, leaderboardNames, discardRule, scheme.getType());
        FlexibleLeaderboard sourceLeaderboard1 = new FlexibleLeaderboardImpl("Leaderboard1", null, scheme, null);
        sourceService.addLeaderboard(sourceLeaderboard1);
        sourceGroup.addLeaderboard(sourceLeaderboard1);

        LeaderboardGroupMetaLeaderboard metaLeaderboard = (LeaderboardGroupMetaLeaderboard) sourceGroup
                .getOverallLeaderboard();
        double factor = 2.6;
        metaLeaderboard.getRaceColumns().iterator().next().setFactor(factor);

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(sourceGroup.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        ByteArrayInputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

        // Test correct number of creations
        Assert.assertEquals(1, creationCount.getLeaderboardGroupCount());

        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        LeaderboardGroupMetaLeaderboard overallLeaderboard = (LeaderboardGroupMetaLeaderboard) leaderboardGroupOnTarget
                .getOverallLeaderboard();
        Assert.assertNotNull(overallLeaderboard);
        Leaderboard overallLeaderboardRetrievedByName = destService.getLeaderboardByName(overallLeaderboard.getName());
        assertSame(overallLeaderboard, overallLeaderboardRetrievedByName);

        Assert.assertNotNull(overallLeaderboard.getResultDiscardingRule());

        Assert.assertNotNull(overallLeaderboard.getScoringScheme());

        Assert.assertEquals(scheme.getType(), overallLeaderboard.getScoringScheme().getType());

        Assert.assertEquals(3, ((ThresholdBasedResultDiscardingRule) overallLeaderboard.getResultDiscardingRule())
                .getDiscardIndexResultsStartingWithHowManyRaces()[2]);

        Iterable<RaceColumn> metaColumns = overallLeaderboard.getRaceColumns();

        RaceColumn metaColumn = metaColumns.iterator().next();
        Assert.assertNotNull(metaColumn);
        Assert.assertEquals(factor, overallLeaderboard.getScoringScheme().getScoreFactor(metaColumn));

        // Verify that overall leaderboard data has been persisted
        RacingEventService persistenceVerifier = new RacingEventServiceImplMock(){};
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
        Assert.assertEquals(factor, overallLeaderboard.getScoringScheme().getScoreFactor(metaColumn));

    }

    @Test
    public void testMasterDataImportWithFlexibleLeaderboard() throws MalformedURLException, IOException,
            InterruptedException, ClassNotFoundException {
        // Setup source service
        RacingEventService sourceService = new RacingEventServiceImpl();
        UUID courseAreaUUID = UUID.randomUUID();

        List<String> raceColumnNames = new ArrayList<String>();
        String raceColumnName = "T1";
        raceColumnNames.add(raceColumnName);
        raceColumnNames.add("T2");
        final List<String> emptyRaceColumnNamesList = Collections.emptyList();

        List<Series> seriesOnSource = new ArrayList<>();
        List<Series> seriesOnTarget = new ArrayList<>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        String testFleet1Name = "testFleet1";
        FleetImpl testFleet1 = new FleetImpl(testFleet1Name);
        fleets.add(testFleet1);
        fleets.add(new FleetImpl("testFleet2"));
        seriesOnSource.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        seriesOnTarget.add(new SeriesImpl("testSeries", false, /* isFleetsCanRunInParallel */ true, fleets, emptyRaceColumnNamesList, sourceService));
        UUID regattaUUID = UUID.randomUUID();
        Regatta regatta = sourceService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME, 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */ null, /* endDate */null,
                regattaUUID, seriesOnSource, true, new LowPoint(), courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0,
                /* useStartTimeInference */true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        for (String name : raceColumnNames) {
            seriesOnSource.get(0).addRaceColumn(name, sourceService);
        }
        double factor = 3.0;
        seriesOnSource.get(0).getRaceColumnByName(raceColumnName).setFactor(factor);

        int[] discardRule = { 1, 2, 3, 4 };
        ScoringScheme scoringScheme = new LowPoint();
        String flexLeaderboardName = "FlexName";
        FlexibleLeaderboard leaderboard = sourceService.addFlexibleLeaderboard(flexLeaderboardName, "TestFlex",
                discardRule, scoringScheme, courseAreaUUID);
        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(leaderboard.getName());
        LeaderboardGroup group = sourceService.addLeaderboardGroup(UUID.randomUUID(), TEST_GROUP_NAME, "testGroupDesc",
        /* displayName */null, false, leaderboardNames, null, null);

        // Set tracked Race with competitors
        List<Competitor> competitors = new ArrayList<Competitor>();
        UUID competitorUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        sailors.add(new PersonImpl("Froderik Poterson", new NationalityImpl("GER"), new Date(645487200000L),
                "Oberhoschy"));
        DynamicPerson coach = new PersonImpl("Lennart Hensler", new NationalityImpl("GER"), new Date(645487200000L),
                "Der Lennart halt");
        DynamicTeam team = new TeamImpl("Pros", sailors, coach);
        BoatClass boatClass = new BoatClassImpl("H16", true);
        CompetitorImpl competitor = new CompetitorImpl(competitorUUID, "Froderik", "KYC", Color.RED, null, null, team, /* timeOnTimeFactor */
                null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitor);
        UUID competitorToSuppressUUID = UUID.randomUUID();
        Set<DynamicPerson> sailors2 = new HashSet<DynamicPerson>();
        sailors2.add(new PersonImpl("Angela Merkel", new NationalityImpl("GER"), new Date(645487200000L),
                "segelt auch mit"));
        DynamicPerson coach2 = new PersonImpl("Peer Steinbrueck", new NationalityImpl("GER"), new Date(645487200000L),
                "Bester Coach");
        DynamicTeam team2 = new TeamImpl("Noobs", sailors2, coach2);
        CompetitorImpl competitorToSuppress = new CompetitorImpl(competitorToSuppressUUID, "Merkel", "KYC", Color.RED, null,
                null, team2, /* timeOnTimeFactor */null, /* timeOnDistanceAllowancePerNauticalMile */null, null);
        competitors.add(competitorToSuppress);
        TrackedRace trackedRace = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors), regatta, null, sourceService.getWindStore());

        leaderboard.addRace(trackedRace, raceColumnName, false);
        RaceColumn raceColumnOnLeaderboard = leaderboard.getRaceColumnByName(raceColumnName);
        raceColumnOnLeaderboard.setFactor(3.0);

        // Set log event
        TimePoint logTimePoint = new MillisecondsTimePoint(1372489200000L);
        TimePoint logTimePoint2 = new MillisecondsTimePoint(1372489201000L);
        RaceLogStartTimeEvent logEvent = new RaceLogStartTimeEventImpl(logTimePoint, author, 1, logTimePoint);
        Fleet defaultFleet = leaderboard.getFleet(null);
        raceColumnOnLeaderboard.getRaceLog(defaultFleet).add(logEvent);
        Position p = new DegreePosition(3, 3);
        Wind wind = new WindImpl(p, logTimePoint2, new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(12)));
        RaceLogWindFixEvent windEvent = new RaceLogWindFixEventImpl(logTimePoint2, author, 2, wind, /* isMagnetic */false);
        raceColumnOnLeaderboard.getRaceLog(defaultFleet).add(windEvent);
        storedLogUUIDs.add(logEvent.getId());
        storedLogUUIDs.add(windEvent.getId());

        // Set score correction
        double scoreCorrection = 12.0;
        leaderboard.getScoreCorrection().correctScore(competitor, raceColumnOnLeaderboard, scoreCorrection);
        MaxPointsReason maxPointsReason = MaxPointsReason.DNS;
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumnOnLeaderboard, maxPointsReason);

        // Set carried Points
        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        // Set suppressed competitor
        leaderboard.setSuppressed(competitorToSuppress, true);

        // Set display name
        String nickName = "Angie";
        leaderboard.setDisplayName(competitorToSuppress, nickName);

        // Serialize
        List<String> groupNamesToExport = new ArrayList<String>();
        groupNamesToExport.add(group.getName());

        RacingEventService destService;
        DomainFactory domainFactory;
        MasterDataResource resource = new MasterDataResource();
        MasterDataResource spyResource = spyResource(resource, sourceService);
        Response response = spyResource.getMasterDataByLeaderboardGroups(groupNamesToExport, false, true, false);
        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        UUID randomUUID = UUID.randomUUID();
        InputStream inputStream = null;
        try {
            streamingOutput.write(os);
            os.flush();
            // Delete all data above from the database, to allow recreating all of it on target server
            deleteAllDataFromDatabase();
            // Import in new service
            destService = new RacingEventServiceImplMock(new DataImportProgressImpl(randomUUID)){};
            domainFactory = destService.getBaseDomainFactory();
            inputStream = new ByteArrayInputStream(os.toByteArray());

            MasterDataImporter importer = new MasterDataImporter(domainFactory, destService);
            importer.importFromStream(inputStream, randomUUID, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            os.close();
            inputStream.close();
        }

        MasterDataImportObjectCreationCount creationCount = destService.getDataImportLock().getProgress(randomUUID)
                .getResult();

        Assert.assertNotNull(creationCount);
        LeaderboardGroup leaderboardGroupOnTarget = destService.getLeaderboardGroupByName(TEST_GROUP_NAME);
        Assert.assertNotNull(leaderboardGroupOnTarget);
        Leaderboard leaderboardOnTarget = destService.getLeaderboardByName(flexLeaderboardName);
        Assert.assertNotNull(leaderboardOnTarget);

        RaceColumn raceColumnOnTarget = leaderboardOnTarget.getRaceColumns().iterator().next();
        Fleet defaultFleetOnTarget = raceColumnOnTarget.getFleets().iterator().next();
        Assert.assertNotNull(raceColumnOnTarget);
        Assert.assertNull(raceColumnOnTarget.getTrackedRace(defaultFleetOnTarget));

        Regatta regattaOnTarget = destService.createRegatta(
                RegattaImpl.getDefaultName(TEST_REGATTA_NAME, TEST_BOAT_CLASS_NAME), TEST_BOAT_CLASS_NAME,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */null, /* endDate */null,
                regattaUUID, seriesOnTarget, true, new LowPoint(),
                courseAreaUUID, /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);

        raceColumnOnTarget.setTrackedRace(defaultFleetOnTarget, new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitors),
                regattaOnTarget, null, sourceService.getWindStore()));

        Assert.assertTrue(leaderboardOnTarget.getScoreCorrection().hasCorrectionFor(raceColumnOnTarget));
        Competitor competitorOnTarget = domainFactory.getExistingCompetitorById(competitorUUID);
        Set<Competitor> competitorsCreatedOnTarget = new HashSet<Competitor>();
        competitorsCreatedOnTarget.add(competitorOnTarget);

        Assert.assertEquals(defaultFleetOnTarget, leaderboardOnTarget.getFleet(null));
        TrackedRace trackedRaceForTarget = new DummyTrackedRace(raceId, createCompetitorsAndBoatsMap(boatClass, competitorsCreatedOnTarget), regattaOnTarget,
                null, sourceService.getWindStore());

        raceColumnOnTarget.setTrackedRace(defaultFleetOnTarget, trackedRaceForTarget);

        Assert.assertEquals(factor, leaderboardOnTarget.getScoringScheme().getScoreFactor(raceColumnOnTarget));

        // Check for score corrections
        Assert.assertEquals(
                scoreCorrection,
                leaderboardOnTarget.getScoreCorrection().getExplicitScoreCorrection(competitorOnTarget,
                        raceColumnOnTarget));
        Assert.assertEquals(maxPointsReason, leaderboardOnTarget.getScoreCorrection()
                .getMaxPointsReason(competitorOnTarget, raceColumnOnTarget, MillisecondsTimePoint.now()));

        // Check for carried points
        Assert.assertEquals(carriedPoints, leaderboardOnTarget.getCarriedPoints(competitorOnTarget));

        // Check for suppressed competitor
        Assert.assertTrue(leaderboardOnTarget.getSuppressedCompetitors().iterator().hasNext());
        Competitor suppressedCompetitorOnTarget = domainFactory.getCompetitorAndBoatStore().getExistingCompetitorById(
                competitorToSuppressUUID);
        Assert.assertEquals(suppressedCompetitorOnTarget, leaderboardOnTarget.getSuppressedCompetitors().iterator()
                .next());

        // Check for competitor display name
        Assert.assertEquals(nickName, leaderboardOnTarget.getDisplayName(suppressedCompetitorOnTarget));

        // Check for race log event
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(defaultFleetOnTarget).getFirstRawFixAtOrAfter(logTimePoint));
        Assert.assertEquals(logEvent.getId(), raceColumnOnTarget.getRaceLog(defaultFleetOnTarget)
                .getFirstRawFixAtOrAfter(logTimePoint).getId());
        Assert.assertNotNull(raceColumnOnTarget.getRaceLog(defaultFleetOnTarget).getFirstFixAtOrAfter(logTimePoint2));
        Assert.assertEquals(wind, ((RaceLogWindFixEvent) raceColumnOnTarget.getRaceLog(defaultFleetOnTarget)
                .getFirstFixAtOrAfter(logTimePoint2)).getWindFix());

        // Check for persisting of race log events:
        RacingEventService dest2 = new RacingEventServiceImplMock(){};
        Leaderboard lb2 = dest2.getLeaderboardByName(flexLeaderboardName);
        RaceColumn raceColumn2 = lb2.getRaceColumns().iterator().next();
        RaceLog raceLog2 = raceColumn2.getRaceLog(raceColumn2.getFleets().iterator().next());
        Assert.assertEquals(logEvent.getId(), raceLog2.getFirstRawFixAtOrAfter(logTimePoint).getId());
    }

}
