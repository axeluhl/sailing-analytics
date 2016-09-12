package com.sap.sailing.domain.racelogtracking.test.impl;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.CollectionNames;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.Timed;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SensorFixStoreTest {
    private static final long FIX_TIMESTAMP = 110;
    private static final long FIX_TIMESTAMP2 = 120;
    private static final double FIX_RIDE_HEIGHT = 1337.0;
    private static final double FIX_RIDE_HEIGHT2 = 1338.0;
    protected final MockSmartphoneImeiServiceFinderFactory serviceFinderFactory = new MockSmartphoneImeiServiceFinderFactory();
    protected final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");
    protected final DeviceIdentifier device2 = new SmartphoneImeiIdentifier("b");
    protected SensorFixStore store;

    protected GPSFixMoving createFix(long millis, double lat, double lng, double knots, double degrees) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng), new MillisecondsTimePoint(millis),
                new KnotSpeedWithBearingImpl(knots, new DegreeBearingImpl(degrees)));
    }

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        dropPersistedData();
        newStore();

    }

    private void newStore() {
        store = new MongoSensorFixStoreImpl(PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), serviceFinderFactory);
    }

    @After
    public void after() {
        dropPersistedData();
    }

    private void dropPersistedData() {
        DB db = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase();
        db.getCollection(CollectionNames.GPS_FIXES.name()).drop();
        db.getCollection(CollectionNames.GPS_FIXES_METADATA.name()).drop();
    }

    @Test
    public void testFixIsPersisted() throws Exception {
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        assertEquals(1, store.getNumberOfFixes(device));
    }

    /**
     * Ensures that no local caching in the store makes in seem to work but data will be lost on restart of the system.
     */
    @Test
    public void testFixIsFoundByOtherStoreInstance() throws Exception {
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);

        newStore();
        assertEquals(1, store.getNumberOfFixes(device));
    }
    
    @Test
    public void testFixDataIsPreservedOnStore() throws Exception {
        DoubleVectorFix fix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        
        verifySingleFix(fix, 100, 200, device, true);
    }
    
    @Test
    public void testFixWithinExclusiveBoundsIsLoaded() throws Exception {
        DoubleVectorFix fix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        verifySingleFix(fix, FIX_TIMESTAMP - 1, FIX_TIMESTAMP + 1, device, false);
    }
    
    @Test
    public void testFixOnInclusiveLowerBoundIsLoaded() throws Exception {
        DoubleVectorFix fix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        verifySingleFix(fix, FIX_TIMESTAMP, FIX_TIMESTAMP + 1, device, true);
    }
    
    @Test
    public void testFixOnInclusiveUpperBoundIsLoaded() throws Exception {
        DoubleVectorFix fix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        verifySingleFix(fix, FIX_TIMESTAMP-1, FIX_TIMESTAMP, device, true);
    }
    
    @Test
    public void testFixesOnExclusiveBoundsArentLoaded() throws Exception {
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        addBravoFix(device, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);
        verifyNoFix(FIX_TIMESTAMP, FIX_TIMESTAMP2, device, false);
    }

    private void verifySingleFix(Timed expectedFix, long start, long end, DeviceIdentifier device, boolean inclusive) throws Exception {
        List<Timed> loadedFixes = loadFixes(start, end, device, inclusive);
        assertEquals(1, loadedFixes.size());
        assertEquals(expectedFix, loadedFixes.get(0));
    }
    
    private void verifyNoFix(long start, long end, DeviceIdentifier device, boolean inclusive) throws Exception {
        List<Timed> loadedFixes = loadFixes(start, end, device, inclusive);
        assertEquals(0, loadedFixes.size());
    }

    private List<Timed> loadFixes(long start, long end, DeviceIdentifier device, boolean inclusive)
            throws TransformationException {
        List<Timed> loadedFixes = new ArrayList<>();
        
        store.loadFixes(loadedFixes::add, device, new MillisecondsTimePoint(start), new MillisecondsTimePoint(end), inclusive);
        return loadedFixes;
    }

    private DoubleVectorFix addBravoFix(DeviceIdentifier device, long timestamp, double rideHeight) {
        DoubleVectorFix fix = createBravoDoubleVectorFixWithRideHeight(timestamp, rideHeight);
        store.storeFix(device, fix);
        return fix;
    }

    private DoubleVectorFix createBravoDoubleVectorFixWithRideHeight(long timestamp, double rideHeight) {
        double[] fixData = new double[BravoSensorDataMetadata.INSTANCE.getColumns().size()];
        fixData[BravoSensorDataMetadata.INSTANCE.rideHeightColumn] = rideHeight;
        return new DoubleVectorFixImpl(new MillisecondsTimePoint(timestamp), fixData);
    }
}
