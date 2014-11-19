package com.sap.sailing.domain.racelogtracking.test;

import static com.sap.sse.common.Util.size;
import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoGPSFixStoreImpl;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockDeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
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
    protected GPSFixStore store;
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null);
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");

    private final RaceLogEventAuthor author = new RaceLogEventAuthorImpl("author", 0);

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng),
                new MillisecondsTimePoint(millis), new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }

    @Before
    public void setServiceAndRaceLog() {
        service = new RacingEventServiceImpl(null, null, serviceFinderFactory);
        raceLog = new RaceLogImpl("racelog");
        store = new MongoGPSFixStoreImpl(service.getMongoObjectFactory(), service.getDomainObjectFactory(),
                serviceFinderFactory);
    }

    @After
    public void after() {
        MongoObjectFactoryImpl mongoOF = (MongoObjectFactoryImpl) service.getMongoObjectFactory();
        mongoOF.getGPSFixCollection().drop();
        mongoOF.getGPSFixMetadataCollection().drop();
    }

    protected void map(RaceLog raceLog, Competitor comp, DeviceIdentifier device, long from, long to) {
        raceLog.add(RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(MillisecondsTimePoint.now(), author, device,
                comp, 0, new MillisecondsTimePoint(from), new MillisecondsTimePoint(to)));
    }

    protected void map(Competitor comp, DeviceIdentifier device, long from, long to) {
        map(raceLog, comp, device, from, to);
    }

    protected void map(Mark mark, DeviceIdentifier device, long from, long to) {
        raceLog.add(RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(MillisecondsTimePoint.now(), author, device,
                mark, 0, new MillisecondsTimePoint(from), new MillisecondsTimePoint(to)));
    }

    protected void testLength(Track<?> track, long expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }
}
