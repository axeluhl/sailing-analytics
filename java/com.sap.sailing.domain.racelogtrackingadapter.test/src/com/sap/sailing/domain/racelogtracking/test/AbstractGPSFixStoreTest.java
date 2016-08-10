package com.sap.sailing.domain.racelogtracking.test;

import static com.sap.sse.common.Util.size;
import static junit.framework.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockDeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbstractGPSFixStoreTest {
    protected RacingEventService service;
    protected final  MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer =
            new MockDeviceAndSessionIdentifierWithGPSFixesDeserializer();
    protected final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
    protected RaceLog raceLog;
    protected RegattaLog regattaLog;
    protected SensorFixStore store;
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null, null, null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");

    protected final AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng),
                new MillisecondsTimePoint(millis), new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }

    @Before
    public void setServiceAndRaceLog() {
        service = new RacingEventServiceImpl(null, null, serviceFinderFactory);
        raceLog = new RaceLogImpl("racelog");
        regattaLog = new RegattaLogImpl("regattalog");
        
        store = new MongoSensorFixStoreImpl(service.getMongoObjectFactory(), service.getDomainObjectFactory(),
                serviceFinderFactory);
    }

    @After
    public void after() {
        MongoObjectFactoryImpl mongoOF = (MongoObjectFactoryImpl) service.getMongoObjectFactory();
        mongoOF.getGPSFixCollection().drop();
        mongoOF.getGPSFixMetadataCollection().drop();
    }

    protected void map(RegattaLog regattaLog, Competitor comp, DeviceIdentifier device, long from, long to) {
        regattaLog.add(new RegattaLogDeviceCompetitorMappingEventImpl(MillisecondsTimePoint.now(), MillisecondsTimePoint.now(), author, UUID.randomUUID(),
                comp, device, new MillisecondsTimePoint(from), new MillisecondsTimePoint(to)));
    }

    protected void map(Competitor comp, DeviceIdentifier device, long from, long to) {
        map(regattaLog, comp, device, from, to);
    }

    protected void map(Mark mark, DeviceIdentifier device, long from, long to) {
        regattaLog.add(new RegattaLogDeviceMarkMappingEventImpl(MillisecondsTimePoint.now(), MillisecondsTimePoint.now(), author, UUID.randomUUID(),
                mark, device, new MillisecondsTimePoint(from), new MillisecondsTimePoint(to)));
    }

    protected void testNumberOfRawFixes(Track<?> track, long expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }
}
