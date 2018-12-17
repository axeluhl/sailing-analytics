package com.sap.sailing.domain.racelogtracking.test;

import static com.sap.sse.common.Util.size;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockDeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbstractGPSFixStoreTest extends RaceLogTrackingTestHelper {
    protected RacingEventService service;
    protected final  MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer =
            new MockDeviceAndSessionIdentifierWithGPSFixesDeserializer();
    protected final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
    protected RegattaLog regattaLog;
    protected SensorFixStore store;
    protected final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null, null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Boat boat = DomainFactory.INSTANCE.getOrCreateBoat("boat", "boat", boatClass, "GER 234", null);
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng),
                new MillisecondsTimePoint(millis), new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }
    
    protected GPSFix createFix(long millis, double lat, double lng) {
        return new GPSFixImpl(new DegreePosition(lat, lng),
                new MillisecondsTimePoint(millis));
    }

    @Before
    public void setServiceAndRaceLog() {
        service = new RacingEventServiceImpl(null, null, serviceFinderFactory);
        raceLog = new RaceLogImpl("racelog");
        regattaLog = new RegattaLogImpl("regattalog");
        dropPersistedData();
        store = new MongoSensorFixStoreImpl(service.getMongoObjectFactory(), service.getDomainObjectFactory(),
                serviceFinderFactory);
    }

    @After
    public void after() {
        dropPersistedData();
    }

    private void dropPersistedData() {
        MongoObjectFactoryImpl mongoOF = (MongoObjectFactoryImpl) service.getMongoObjectFactory();
        mongoOF.getGPSFixCollection().drop();
        mongoOF.getGPSFixMetadataCollection().drop();
        mongoOF.getRaceLogCollection().drop();
        mongoOF.getRegattaLogCollection().drop();
    }

    protected void map(RegattaLog regattaLog, Competitor comp, DeviceIdentifier device, long from, long to) {
        regattaLog.add(new RegattaLogDeviceCompetitorMappingEventImpl(MillisecondsTimePoint.now(),
                MillisecondsTimePoint.now(), author, UUID.randomUUID(), comp, device, new MillisecondsTimePoint(from),
                new MillisecondsTimePoint(to)));
    }

    protected void map(Competitor comp, DeviceIdentifier device, long from, long to) {
        map(regattaLog, comp, device, from, to);
    }

    protected void map(Mark mark, DeviceIdentifier device, long from, long to) {
        regattaLog.add(new RegattaLogDeviceMarkMappingEventImpl(MillisecondsTimePoint.now(),
                MillisecondsTimePoint.now(), author, UUID.randomUUID(), mark, device, new MillisecondsTimePoint(from),
                new MillisecondsTimePoint(to)));
    }
    
    protected void defineMarksOnRegattaLog(Mark... marks) {
        for (int i = 0; i < marks.length; i++) {
            regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(i + 1), author,
                    new MillisecondsTimePoint(1), 0, marks[i]));
        }
    }

    protected DynamicTrackedRaceImpl createDynamicTrackedRace(BoatClass boatClass, RaceDefinition raceDefinition) {
        DynamicTrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass,
                /* canBoatsOfCompetitorsChangePerRace */ true,  CompetitorRegistrationType.CLOSED, /* startDate */ null, /* endDate */null, null, null, "a", null));
        return new DynamicTrackedRaceImpl(regatta, raceDefinition, Collections.<Sideline> emptyList(),
                EmptyWindStore.INSTANCE, 0, 0, 0, /* useMarkPassingCalculator */ false, OneDesignRankingMetric::new,
                mock(RaceLogResolver.class));
    }

    protected void testNumberOfRawFixes(Track<?> track, long expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }
}
