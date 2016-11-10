package com.sap.sailing.domain.racelogtracking.test.impl;

import static com.sap.sse.common.Util.size;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRevokeEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.BravoDataFixMapper;
import com.sap.sailing.domain.racelogtracking.impl.fixtracker.FixLoaderAndTracker;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.SensorFixTrackImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SensorFixStoreAndLoadTest {
    private static final long START_OF_TRACKING = 100;
    private static final long MID_OF_TRACKING = 200;
    private static final long END_OF_TRACKING = 300;
    private static final long FIX_TIMESTAMP = 110;
    private static final long FIX_TIMESTAMP2 = 120;
    private static final long FIX_TIMESTAMP3 = 210;
    private static final long AFTER_LAST_FIX = FIX_TIMESTAMP3 + 1;
    private static final Distance FIX_RIDE_HEIGHT = new MeterDistance(1337.0);
    private static final Distance FIX_RIDE_HEIGHT2 = new MeterDistance(1338.0);
    private static final Distance FIX_RIDE_HEIGHT3 = new MeterDistance(1336.0);
    private static final double FIX_TEST_VALUE = 12.0;
    protected final MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    protected final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
    protected final DeviceIdentifier deviceTest = new SmartphoneImeiIdentifier("b");
    protected RaceLog raceLog;
    protected RegattaLog regattaLog;
    protected SensorFixStore store;
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null, null,
            null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null,
            null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");
    protected final Boat boat1 = DomainFactory.INSTANCE.getOrCreateBoat(comp, "Boat1", boatClass, "GER 1", null);
    protected final Boat boat2 = DomainFactory.INSTANCE.getOrCreateBoat(comp2, "Boat2", boatClass, "GER 2", null);
    
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");
    protected final Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");

    protected final AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
    private DynamicTrackedRace trackedRace;

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng), new MillisecondsTimePoint(millis),
                new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        dropPersistedData();
        raceLog = new RaceLogImpl("racelog");
        raceLog.add(new RaceLogStartOfTrackingEventImpl(new MillisecondsTimePoint(START_OF_TRACKING), author, 0));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(new MillisecondsTimePoint(END_OF_TRACKING), author, 0));
        
        regattaLog = new RegattaLogImpl("regattalog");

        store = new MongoSensorFixStoreImpl(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), serviceFinderFactory);

        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author,
                new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author,
                new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course",
                Arrays.asList(new Waypoint[] { new WaypointImpl(mark), new WaypointImpl(mark2) }));
        Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        competitorsAndBoats.put(comp, boat1);
        competitorsAndBoats.put(comp2, boat2);
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, competitorsAndBoats);
        DynamicTrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass,
                /* startDate */ null, /* endDate */null, null, null, "a", null));
        trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline> emptyList(),
                EmptyWindStore.INSTANCE, 0, 0, 0, /* useMarkPassingCalculator */ false, OneDesignRankingMetric::new,
                mock(RaceLogResolver.class));
    }

    private void dropPersistedData() {
        DB db = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase();
        db.getCollection(CollectionNames.GPS_FIXES.name()).drop();
        db.getCollection(CollectionNames.GPS_FIXES_METADATA.name()).drop();
        db.getCollection(CollectionNames.REGATTA_LOGS.name()).drop();
        db.getCollection(CollectionNames.RACE_LOGS.name()).drop();
    }

    @After
    public void after() {
        dropPersistedData();
    }

    @Test
    public void testLoadAlreadyAddedFixes() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testAddFixesWhileTracking() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addBravoFixes();

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testNoFixesAreLoadedIfNoStoredFixIsInTimeRange() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(AFTER_LAST_FIX), new MillisecondsTimePoint(END_OF_TRACKING)));

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 0);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testFixesNotInMappedTimeRangeAreIgnoredWhileTracking() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(AFTER_LAST_FIX), new MillisecondsTimePoint(END_OF_TRACKING)));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addBravoFixes();

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 0);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testCompetitorWithoutMappingHasNoTrack() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        assertNull(trackedRace.getSensorTrack(comp2, BravoFixTrack.TRACK_NAME));

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testDeviceIsMappedToDifferentCompetitorsInDifferentTimeRanges() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp2,
                device, new MillisecondsTimePoint(MID_OF_TRACKING + 1), new MillisecondsTimePoint(END_OF_TRACKING)));

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);
        testNumberOfRawFixes(trackedRace.getSensorTrack(comp2, BravoFixTrack.TRACK_NAME), 1);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testMultipleMappingsForOneDeviceAndCompetitor() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(MID_OF_TRACKING + 1), new MillisecondsTimePoint(END_OF_TRACKING)));

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 3);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testNothingLoadedForRevokedMapping() throws InterruptedException {
        RegattaLogDeviceCompetitorBravoMappingEventImpl mappingEvent = new RegattaLogDeviceCompetitorBravoMappingEventImpl(
                new MillisecondsTimePoint(3), author, comp, device, new MillisecondsTimePoint(START_OF_TRACKING),
                new MillisecondsTimePoint(MID_OF_TRACKING));
        regattaLog.add(mappingEvent);
        regattaLog.add(new RegattaLogRevokeEventImpl(author, mappingEvent, "Test purposes"));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addBravoFixes();

        trackedRace.waitForLoadingToFinish();

        assertNull(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME));

        fixLoaderAndTracker.stop(true);
    }
    
    @Test
    public void testFixesAreLoadedIfThereIsOneRevokedAndOneNonRevokedMapping() throws InterruptedException {
        // revoked mapping timestamp 100-300
        RegattaLogDeviceCompetitorBravoMappingEventImpl mappingEvent = new RegattaLogDeviceCompetitorBravoMappingEventImpl(
                new MillisecondsTimePoint(3), author, comp, device, new MillisecondsTimePoint(START_OF_TRACKING),
                new MillisecondsTimePoint(END_OF_TRACKING));
        regattaLog.add(mappingEvent);
        regattaLog.add(new RegattaLogRevokeEventImpl(author, mappingEvent, "Test purposes"));
        
        // non-revoked mapping timestamp 100-200
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        
        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();
        
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        
        addBravoFixes();
        
        trackedRace.waitForLoadingToFinish();
        
        // Only Fixes from 100 to 200 may be included
        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);
        
        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testLoadFixesWhenClosingEventIsRevoked() throws InterruptedException {
        UUID mappingEventId = UUID.randomUUID();
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(MillisecondsTimePoint.now(),
                new MillisecondsTimePoint(3), author, mappingEventId, comp, device, new MillisecondsTimePoint(START_OF_TRACKING),
                null));
        RegattaLogCloseOpenEndedDeviceMappingEventImpl closeEvent = new RegattaLogCloseOpenEndedDeviceMappingEventImpl(
                new MillisecondsTimePoint(4), author, mappingEventId, new MillisecondsTimePoint(MID_OF_TRACKING));
        regattaLog.add(closeEvent);

        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME);

        testNumberOfRawFixes(bravoFixTrack, 2);

        regattaLog.add(new RegattaLogRevokeEventImpl(author, closeEvent, "Test purposes"));

        trackedRace.waitForLoadingToFinish();
        testNumberOfRawFixes(bravoFixTrack, 3);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testBravoFixIsCorrectlyWrapped() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        addBravoFixes();
        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        trackedRace.waitForLoadingToFinish();
        BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME);
        assertEquals(FIX_RIDE_HEIGHT,
                bravoFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP)).getRideHeight());
        fixLoaderAndTracker.stop(true);
    }
    
    @Test
    public void testMultipleFixTypesAreLoadedInSeparateTracks() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorTestMappingEventImpl(new MillisecondsTimePoint(1), author, comp,
                deviceTest, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        
        addTestFixes();
        addBravoFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);
        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, TestFixTrackImpl.TRACK_NAME), 1);

        fixLoaderAndTracker.stop(true);
    }
    
    @Test
    public void testMultipleFixTypesAreLoadedInSeparateTracksWhileTracking() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorTestMappingEventImpl(new MillisecondsTimePoint(1), author, comp,
                deviceTest, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addTestFixes();
        addBravoFixes();
        
        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, TestFixTrackImpl.TRACK_NAME), 1);
        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);

        fixLoaderAndTracker.stop(true);
    }
    
    @Test
    public void testMultipleFixTypesAreMappedCorrectly() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorTestMappingEventImpl(new MillisecondsTimePoint(1), author, comp,
                deviceTest, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(END_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(END_OF_TRACKING)));
        

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addTestFixes();
        addBravoFixes();
        
        trackedRace.waitForLoadingToFinish();

        BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME);
        assertEquals(FIX_RIDE_HEIGHT,
                bravoFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP)).getRideHeight());
        assertEquals(FIX_RIDE_HEIGHT2,
                bravoFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP2)).getRideHeight());
        assertEquals(FIX_RIDE_HEIGHT3,
                bravoFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP3)).getRideHeight());
        
        TestFixTrackImpl<Competitor> testFixTrack = trackedRace.getSensorTrack(comp, TestFixTrackImpl.TRACK_NAME);
        assertEquals(FIX_TEST_VALUE,
                testFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP)).getTestValue());

        fixLoaderAndTracker.stop(true);
    }

    private void addBravoFixes() {
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(FIX_TIMESTAMP, FIX_RIDE_HEIGHT.getMeters()));
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2.getMeters()));
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(FIX_TIMESTAMP3, FIX_RIDE_HEIGHT3.getMeters()));
    }

    private FixLoaderAndTracker createFixLoaderAndTracker() {
        
        return new FixLoaderAndTracker(trackedRace, store, new SensorFixMapperFactory() {
            private TestDataFixMapper testDataFixMapper = new TestDataFixMapper();
            private BravoDataFixMapper bravoDataFixMapper = new BravoDataFixMapper();

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public <FixT extends Timed, TrackT extends DynamicTrack<FixT>> SensorFixMapper<FixT, TrackT, Competitor> createCompetitorMapper(
                    Class<? extends RegattaLogDeviceMappingEvent<?>> eventType) {
                if(bravoDataFixMapper.isResponsibleFor(eventType)) {
                    return (SensorFixMapper) bravoDataFixMapper;
                }
                if(testDataFixMapper.isResponsibleFor(eventType)) {
                    return (SensorFixMapper) testDataFixMapper;
                }
                throw new IllegalArgumentException("Unknown event type");
            }
        });
    }

    protected void testNumberOfRawFixes(Track<?> track, long expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }

    private DoubleVectorFix createBravoDoubleVectorFixWithRideHeight(long timestamp, double rideHeight) {
        double[] fixData = new double[BravoSensorDataMetadata.INSTANCE.columnCount];
        fixData[BravoSensorDataMetadata.INSTANCE.rideHeightColumn] = rideHeight;
        return new DoubleVectorFixImpl(new MillisecondsTimePoint(timestamp), fixData);
    }
    
    private void addTestFixes() {
        store.storeFix(deviceTest, createTestDoubleVectorFixWithTestValue(FIX_TIMESTAMP, FIX_TEST_VALUE));
    }
    
    private DoubleVectorFix createTestDoubleVectorFixWithTestValue(long timestamp, double testValue) {
        double[] fixData = new double[TestFixImpl.COLUMNS.size()];
        fixData[TestFixImpl.TEST_COLUMN_INDEX] = testValue;
        return new DoubleVectorFixImpl(new MillisecondsTimePoint(timestamp), fixData);
    }
    
    private static class TestFixImpl implements SensorFix {
        private static final long serialVersionUID = 2033254212013220L;
        
        public static final String TEST_COLUMN = "testColumn";
        public static final int TEST_COLUMN_INDEX = 1;
        
        public static final List<String> COLUMNS = Collections.unmodifiableList(Arrays.asList("blub", TEST_COLUMN));
        
        private final DoubleVectorFix fix;

        public TestFixImpl(DoubleVectorFix fix) {
            this.fix = fix;
        }

        @Override
        public double get(String valueName) {
            return fix.get(COLUMNS.indexOf(valueName));
        }

        @Override
        public TimePoint getTimePoint() {
            return fix.getTimePoint();
        }

        public double getTestValue() {
            return fix.get(TEST_COLUMN_INDEX);
        }

    }
    
    public class TestFixTrackImpl<ItemType extends WithID & Serializable> extends SensorFixTrackImpl<ItemType, TestFixImpl> {
        private static final long serialVersionUID = 5517848726454386L;
        
        public static final String TRACK_NAME = "TestFixTrack";
        
        public TestFixTrackImpl(ItemType trackedItem, String trackName) {
            super(trackedItem, trackName, TestFixImpl.COLUMNS, 
                    TRACK_NAME + " for " + trackedItem);
        }
        
        public Double getTextValue(TimePoint timePoint) {
            TestFixImpl fixAfter = getFirstFixAtOrAfter(timePoint);
            if (fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
                // exact match of timepoint -> no interpolation necessary
                return fixAfter.getTestValue();
            }
            TestFixImpl fixBefore = getLastFixAtOrBefore(timePoint);
            if (fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
                // exact match of timepoint -> no interpolation necessary
                return fixBefore.getTestValue();
            }
            if (fixAfter == null || fixBefore == null) {
                // the fix is out of the TimeRange where we have fixes
                return null;
            }
            return fixBefore.getTestValue();
        }
    }
    
    public class TestDataFixMapper implements SensorFixMapper<TestFixImpl, DynamicSensorFixTrack<Competitor, TestFixImpl>, Competitor> {

        @Override
        public DynamicSensorFixTrack<Competitor, TestFixImpl> getTrack(DynamicTrackedRace race, Competitor key) {
            return race.getOrCreateSensorTrack(key, TestFixTrackImpl.TRACK_NAME, 
                    () -> new TestFixTrackImpl<Competitor>(key, TestFixTrackImpl.TRACK_NAME));
        }
        
        @Override
        public TestFixImpl map(DoubleVectorFix fix) {
            return new TestFixImpl(fix);
        }
        
        @Override
        public boolean isResponsibleFor(Class<? extends RegattaLogDeviceMappingEvent<?>> eventType) {
            return RegattaLogDeviceCompetitorTestMappingEventImpl.class.isAssignableFrom(eventType);
        }
    }
    
    public class RegattaLogDeviceCompetitorTestMappingEventImpl extends RegattaLogDeviceMappingEventImpl<Competitor>
        implements RegattaLogDeviceCompetitorSensorDataMappingEvent {
        private static final long serialVersionUID = -14940305448048753L;
        
        
        public RegattaLogDeviceCompetitorTestMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
                AbstractLogEventAuthor author, Serializable pId, Competitor mappedTo, DeviceIdentifier device,
                TimePoint from, TimePoint to) {
            super(createdAt, logicalTimePoint, author, pId, mappedTo, device, from, to);
        }
        
        public RegattaLogDeviceCompetitorTestMappingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
                Competitor mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
            super(logicalTimePoint, author, mappedTo, device, from, to);
        }
        
        @Override
        public void accept(RegattaLogEventVisitor visitor) {
            visitor.visit(this);
        }
        
        @Override
        public void accept(MappingEventVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    @Test
    public void testThatMappingsOutsideOfTheTrackedIntervalDontCauseLoadingToFail() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(10), new MillisecondsTimePoint(20)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp2,
                deviceTest, new MillisecondsTimePoint(START_OF_TRACKING), new MillisecondsTimePoint(MID_OF_TRACKING)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp2,
                deviceTest, new MillisecondsTimePoint(10), new MillisecondsTimePoint(20)));
        addBravoFixes();
        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        trackedRace.waitForLoadingToFinish();
        assertNotNull(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME));
        assertNotNull(trackedRace.getSensorTrack(comp2, BravoFixTrack.TRACK_NAME));
        fixLoaderAndTracker.stop(true);
    }
}
