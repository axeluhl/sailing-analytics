package com.sap.sailing.domain.racelogtracking.test.impl;

import static com.sap.sse.common.Util.size;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
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
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRevokeEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
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
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
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
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sse.common.Timed;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SensorFixStoreAndLoadTest {
    private static final long FIX_TIMESTAMP = 110;
    private static final double FIX_RIDE_HEIGHT = 1337.0;
    protected final MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    protected final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
    protected RaceLog raceLog;
    protected RegattaLog regattaLog;
    protected SensorFixStore store;
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null,
            null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null,
            null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");
    protected final Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");

    protected final AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
    private DynamicTrackedRace trackedRace;

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng), new MillisecondsTimePoint(millis),
                new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        DB db = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase();
        db.getCollection(CollectionNames.GPS_FIXES.name()).drop();
        db.getCollection(CollectionNames.GPS_FIXES_METADATA.name()).drop();
        raceLog = new RaceLogImpl("racelog");
        regattaLog = new RegattaLogImpl("regattalog");

        store = new MongoSensorFixStoreImpl(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), serviceFinderFactory);

        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author,
                new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author,
                new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course",
                Arrays.asList(new Waypoint[] { new WaypointImpl(mark), new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(comp, comp2));
        DynamicTrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass,
                /* startDate */ null, /* endDate */null, null, null, "a", null));
        trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline> emptyList(),
                EmptyWindStore.INSTANCE, 0, 0, 0, /* useMarkPassingCalculator */ false, OneDesignRankingMetric::new,
                mock(RaceLogResolver.class));
    }

    @After
    public void after() {
        DB db = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase();
        db.getCollection(CollectionNames.GPS_FIXES.name()).drop();
        db.getCollection(CollectionNames.GPS_FIXES_METADATA.name()).drop();
    }

    @Test
    public void testLoadAlreadyAddedFixes() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));

        addFixes();

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
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addFixes();

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testNoFixesAreLoadedIfNoStoredFixIsInTimeRange() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(300), new MillisecondsTimePoint(400)));

        addFixes();

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
                device, new MillisecondsTimePoint(300), new MillisecondsTimePoint(400)));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addFixes();

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 0);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testCompetitorWithoutMappingHasNoTrack() throws InterruptedException {
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));

        addFixes();

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
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp2,
                device, new MillisecondsTimePoint(201), new MillisecondsTimePoint(300)));

        addFixes();

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
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(201), new MillisecondsTimePoint(300)));

        addFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 3);

        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testNothingloadedForRevokedMapping() throws InterruptedException {
        RegattaLogDeviceCompetitorBravoMappingEventImpl mappingEvent = new RegattaLogDeviceCompetitorBravoMappingEventImpl(
                new MillisecondsTimePoint(3), author, comp, device, new MillisecondsTimePoint(100),
                new MillisecondsTimePoint(200));
        regattaLog.add(mappingEvent);
        regattaLog.add(new RegattaLogRevokeEventImpl(author, mappingEvent, "Test purposes"));

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        addFixes();

        trackedRace.waitForLoadingToFinish();

        assertNull(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME));

        fixLoaderAndTracker.stop(true);
    }
    
    @Test
    public void testFixesAreLoadedIfThereIsOneRevokedAndOneNonRevokedMapping() throws InterruptedException {
        // revoked mapping timestamp 100-300
        RegattaLogDeviceCompetitorBravoMappingEventImpl mappingEvent = new RegattaLogDeviceCompetitorBravoMappingEventImpl(
                new MillisecondsTimePoint(3), author, comp, device, new MillisecondsTimePoint(100),
                new MillisecondsTimePoint(300));
        regattaLog.add(mappingEvent);
        regattaLog.add(new RegattaLogRevokeEventImpl(author, mappingEvent, "Test purposes"));
        
        // non-revoked mapping timestamp 100-200
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(new MillisecondsTimePoint(3), author, comp,
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));
        
        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();
        
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        
        addFixes();
        
        trackedRace.waitForLoadingToFinish();
        
        // Only Fixes from 100 to 200 may be included
        testNumberOfRawFixes(trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME), 2);
        
        fixLoaderAndTracker.stop(true);
    }

    @Test
    public void testLoadFixesWhenClosingEventIsRevoked() throws InterruptedException {
        UUID mappingEventId = UUID.randomUUID();
        regattaLog.add(new RegattaLogDeviceCompetitorBravoMappingEventImpl(MillisecondsTimePoint.now(),
                new MillisecondsTimePoint(3), author, mappingEventId, comp, device, new MillisecondsTimePoint(100),
                null));
        RegattaLogCloseOpenEndedDeviceMappingEventImpl closeEvent = new RegattaLogCloseOpenEndedDeviceMappingEventImpl(
                new MillisecondsTimePoint(4), author, mappingEventId, new MillisecondsTimePoint(200));
        regattaLog.add(closeEvent);

        addFixes();

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
                device, new MillisecondsTimePoint(100), new MillisecondsTimePoint(200)));

        addFixes();

        FixLoaderAndTracker fixLoaderAndTracker = createFixLoaderAndTracker();

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);

        trackedRace.waitForLoadingToFinish();

        BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(comp, BravoFixTrack.TRACK_NAME);
        assertEquals(1337.0,
                bravoFixTrack.getFirstFixAtOrAfter(new MillisecondsTimePoint(FIX_TIMESTAMP)).getRideHeight());

        fixLoaderAndTracker.stop(true);
    }

    private void addFixes() {
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(FIX_TIMESTAMP, FIX_RIDE_HEIGHT));
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(120, 1338.0));
        store.storeFix(device, createBravoDoubleVectorFixWithRideHeight(210, 1336.0));
    }

    private FixLoaderAndTracker createFixLoaderAndTracker() {
        return new FixLoaderAndTracker(trackedRace, store, new SensorFixMapperFactory() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public <FixT extends Timed, TrackT extends DynamicTrack<FixT>> SensorFixMapper<FixT, TrackT, Competitor> createCompetitorMapper(
                    Class<? extends RegattaLogDeviceMappingEvent<?>> eventType) {
                return (SensorFixMapper) new BravoDataFixMapper();
            }
        });
    }

    protected void testNumberOfRawFixes(Track<?> track, long expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }

    private DoubleVectorFix createBravoDoubleVectorFixWithRideHeight(long timestamp, double rideHeight) {
        double[] fixData = new double[BravoSensorDataMetadata.INSTANCE.getColumns().size()];
        fixData[BravoSensorDataMetadata.INSTANCE.rideHeightColumn] = rideHeight;
        return new DoubleVectorFixImpl(new MillisecondsTimePoint(timestamp), fixData);
    }
}
