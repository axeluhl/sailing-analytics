package com.sap.sailing.mongodb.test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.test.mock.MockDeviceTypeServiceFinderFactory;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;

import static org.junit.Assert.assertEquals;

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
    public void testStoreAndLoadFixes() {
    	TypeBasedServiceFinderFactory factory = new MockDeviceTypeServiceFinderFactory();
        MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService(), factory);
        DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE, factory);
        
        List<GPSFix> fixes = new ArrayList<>();
        fixes.add(new GPSFixImpl(new DegreePosition(30, 40), new MillisecondsTimePoint(0)));
        fixes.add(new GPSFixImpl(new DegreePosition(40, 50), new MillisecondsTimePoint(1)));
        DeviceIdentifier device1 = new SmartphoneImeiIdentifier("a");
        mongoObjectFactory.storeGPSFixes(device1, fixes);
        
        fixes.clear();
        fixes.add(new GPSFixImpl(new DegreePosition(40, 50), new MillisecondsTimePoint(0)));
        DeviceIdentifier device2 = new SmartphoneImeiIdentifier("b");        
        mongoObjectFactory.storeGPSFixes(device2, fixes);
        
        DynamicTrack<GPSFix> loadedTrack1 = domainObjectFactory.loadGPSFixTrack(device1);
        DynamicTrack<GPSFix> loadedTrack2 = domainObjectFactory.loadGPSFixTrack(device2);
        loadedTrack1.lockForRead();
        assertEquals(2, Util.size(loadedTrack1.getRawFixes()));
        loadedTrack1.unlockAfterRead();
        loadedTrack2.lockForRead();
        assertEquals(1, Util.size(loadedTrack2.getRawFixes()));
        loadedTrack2.unlockAfterRead();
        
        assertEquals(2, domainObjectFactory.loadAllGPSFixTracks().size());
    }
}
