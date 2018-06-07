package com.sap.sailing.mongodb.test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.impl.GPSFixStoreImpl;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StoreGPSFixTrackTest extends AbstractMongoDBTest {
    public StoreGPSFixTrackTest() throws UnknownHostException, MongoException {
        super();
    }

    @After
    public void dropCollection() {
        DB db = getMongoService().getDB();
        db.getCollection(CollectionNames.GPS_FIXES.name()).drop();
    }
    
    @Test
    public void testStoreAndLoadFixes() throws TransformationException, NoCorrespondingServiceRegisteredException {
    	TypeBasedServiceFinderFactory factory = new MockSmartphoneImeiServiceFinderFactory();
    	AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
    	Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", "c", null, null, null, null,
    	    /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    	Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService(), factory);
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE, factory);
        GPSFixStore store = new GPSFixStoreImpl(new MongoSensorFixStoreImpl(mongoObjectFactory, domainObjectFactory, factory));
        
        TimePoint time0 = new MillisecondsTimePoint(0);
        TimePoint time1 = new MillisecondsTimePoint(1);
        TimePoint time2 = new MillisecondsTimePoint(2);
        
        GPSFixMoving fix1 = new GPSFixMovingImpl(new DegreePosition(30, 40), time0,
        		new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0)));
        GPSFixMoving fix2 = new GPSFixMovingImpl(new DegreePosition(30, 40), time1,
        		new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0)));
        //lies outside of mapping range
        GPSFixMoving fix3 = new GPSFixMovingImpl(new DegreePosition(30, 40), time2,
        		new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0)));
        DeviceIdentifier device1 = new SmartphoneImeiIdentifier("a");
        store.storeFix(device1, fix1);
        store.storeFix(device1, fix2);
        store.storeFix(device1, fix3);
        
        GPSFix fix4 = new GPSFixImpl(new DegreePosition(40, 50), time0);
        DeviceIdentifier device2 = new SmartphoneImeiIdentifier("b");
        store.storeFix(device2, fix4);
        
        RegattaLog regattaLog = new RegattaLogImpl("racelog");
        regattaLog.add(new RegattaLogDeviceCompetitorMappingEventImpl(time0, author, comp, device1, time0, time1));
        regattaLog.add(new RegattaLogDeviceMarkMappingEventImpl(time0, author, mark, device2, time0, time1));
        
        DynamicGPSFixMovingTrackImpl<Competitor> track1 = new DynamicGPSFixMovingTrackImpl<>(comp, 0);
        store.loadCompetitorTrack(track1, regattaLog, comp);
        DynamicGPSFixTrackImpl<Mark> track2 = new DynamicGPSFixTrackImpl<>(mark, 0);
        store.loadMarkTrack(track2, regattaLog, mark);
        track1.lockForRead();
        assertEquals(2, Util.size(track1.getRawFixes()));
        assertTrue(track1.getFirstRawFix() instanceof GPSFixMoving);
        track1.unlockAfterRead();
        track2.lockForRead();
        assertEquals(1, Util.size(track2.getRawFixes()));
        track2.unlockAfterRead();
    }
}
