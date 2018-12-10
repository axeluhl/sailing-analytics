package com.sap.sailing.domain.racelogtracking.test.impl;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.common.DeviceIdentifier;
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
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneImeiServiceFinderFactory;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Timed;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

import org.junit.Assert;

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
    public void testProgressSingle() throws Exception {
        testProgress(1);
    }
    
    @Test
    public void testProgressFew() throws Exception {
        testProgress(80);
    }
    
    @Test
    public void testProgressLittle() throws Exception {
        testProgress(109);
    }

    @Test
    public void testProgressMany() throws Exception {
        testProgress(2000);
    }

    private void testProgress(int fixes) throws NoCorrespondingServiceRegisteredException, TransformationException {
        for (int i = 0; i < fixes; i++) {
            addBravoFix(device, FIX_TIMESTAMP + 1, FIX_RIDE_HEIGHT);
        }

        List<Double> progressData = new ArrayList<>();
        store.loadFixes((fix) -> {
        }, device, new MillisecondsTimePoint(FIX_TIMESTAMP - 1), new MillisecondsTimePoint(FIX_TIMESTAMP + fixes + 1),
                true, () -> false, new Consumer<Double>() {

                    @Override
                    public void accept(Double progress) {
                        progressData.add(progress);
                    }
                });

        // validate monotone rising
        // validate between 0 and 1 (inclusive)
        Double last = 0.0;
        for (Double progress : progressData) {
            Assert.assertTrue(progress <= 1);
            Assert.assertTrue(progress >= 0);
            Assert.assertTrue(progress >= last);
            last = progress;
        }
        //validate that 0-100 updates do exist
        Assert.assertFalse(progressData.isEmpty());
        Assert.assertTrue(progressData.size() <= 100);
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
        verifySingleFix(fix, FIX_TIMESTAMP - 1, FIX_TIMESTAMP, device, true);
    }

    @Test
    public void testFixesOnExclusiveEndBoundsArentLoaded() throws Exception {
        DoubleVectorFix fix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        addBravoFix(device, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);
        verifySingleFix(fix, FIX_TIMESTAMP, FIX_TIMESTAMP2, device, false);
    }
    
    @Test
    public void testYoungestFix() throws Exception {
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        addBravoFix(device, FIX_TIMESTAMP + 2, FIX_RIDE_HEIGHT2);
        DoubleVectorFix youngestFix = addBravoFix(device, FIX_TIMESTAMP + 5, FIX_RIDE_HEIGHT2);
        verifyYoungestFix(youngestFix, FIX_TIMESTAMP, FIX_TIMESTAMP2, device);
    }
    
    @Test
    public void testOldestFix() throws Exception {
        DoubleVectorFix oldestFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        addBravoFix(device, FIX_TIMESTAMP + 2, FIX_RIDE_HEIGHT2);
        addBravoFix(device, FIX_TIMESTAMP + 5, FIX_RIDE_HEIGHT2);
        verifyOldestFix(oldestFix, FIX_TIMESTAMP, FIX_TIMESTAMP2, device);
    }

    @Test
    public void testSingleListenerIsNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);

        Mockito.verify(listener, Mockito.times(1)).fixReceived(device, doubleVectorFix);
    }

    @Test
    public void testMultipleListenersForSameDeviceAreNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener1 = mockFixReceivedListener();
        FixReceivedListener<DoubleVectorFix> listener2 = mockFixReceivedListener();
        store.addListener(listener1, device);
        store.addListener(listener2, device);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);

        Mockito.verify(listener1, Mockito.times(1)).fixReceived(device, doubleVectorFix);
        Mockito.verify(listener2, Mockito.times(1)).fixReceived(device, doubleVectorFix);
    }

    @Test
    public void testListenerForNonMatchingDeviceIsNotNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        addBravoFix(device2, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);

        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testOnlyListenerForMatchingDeviceIsNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener1 = mockFixReceivedListener();
        FixReceivedListener<DoubleVectorFix> listener2 = mockFixReceivedListener();
        store.addListener(listener1, device);
        store.addListener(listener2, device2);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);

        Mockito.verify(listener1, Mockito.times(1)).fixReceived(device, doubleVectorFix);
        Mockito.verifyZeroInteractions(listener2);
    }

    @Test
    public void testListenerForMatchingDeviceIsNotifiedMultipleTimes() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        DoubleVectorFix doubleVectorFix1 = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        DoubleVectorFix doubleVectorFix2 = addBravoFix(device, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);

        Mockito.verify(listener, Mockito.times(1)).fixReceived(device, doubleVectorFix1);
        Mockito.verify(listener, Mockito.times(1)).fixReceived(device, doubleVectorFix2);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAfterRemoveListenerIsNotNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.removeListener(listener);
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testAfterRemoveForDeviceListenerIsNotNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.removeListener(listener, device);
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testAfterRemoveForNonMatchingDeviceListenerIsNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.removeListener(listener, device2);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        Mockito.verify(listener, Mockito.times(1)).fixReceived(device, doubleVectorFix);
    }

    @Test
    public void testListenerForMultipleDevicesIsNotifiedMultipleTimes() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.addListener(listener, device2);
        DoubleVectorFix doubleVectorFix1 = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        DoubleVectorFix doubleVectorFix2 = addBravoFix(device2, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);
        Mockito.verify(listener, Mockito.times(1)).fixReceived(device, doubleVectorFix1);
        Mockito.verify(listener, Mockito.times(1)).fixReceived(device2, doubleVectorFix2);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAfterRemoveListenerForMultipleDevicesIsNotNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.addListener(listener, device2);
        store.removeListener(listener);
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        addBravoFix(device2, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testAfterRemoveListenerForOneDevicesIsNotifiedForOtherDevice() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener = mockFixReceivedListener();
        store.addListener(listener, device);
        store.addListener(listener, device2);
        store.removeListener(listener, device);
        addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        DoubleVectorFix doubleVectorFix2 = addBravoFix(device2, FIX_TIMESTAMP2, FIX_RIDE_HEIGHT2);
        Mockito.verify(listener, Mockito.times(1)).fixReceived(device2, doubleVectorFix2);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAfterRemoveListenerOtherListenerIsNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener1 = mockFixReceivedListener();
        FixReceivedListener<DoubleVectorFix> listener2 = mockFixReceivedListener();
        store.addListener(listener1, device);
        store.addListener(listener2, device);
        store.removeListener(listener1);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        Mockito.verify(listener2, Mockito.times(1)).fixReceived(device, doubleVectorFix);
        Mockito.verifyNoMoreInteractions(listener2);
        Mockito.verifyZeroInteractions(listener1);
    }

    @Test
    public void testAfterRemoveForDeviceListenerOtherListenerIsNotified() throws Exception {
        FixReceivedListener<DoubleVectorFix> listener1 = mockFixReceivedListener();
        FixReceivedListener<DoubleVectorFix> listener2 = mockFixReceivedListener();
        store.addListener(listener1, device);
        store.addListener(listener2, device);
        store.removeListener(listener1, device);
        DoubleVectorFix doubleVectorFix = addBravoFix(device, FIX_TIMESTAMP, FIX_RIDE_HEIGHT);
        Mockito.verify(listener2, Mockito.times(1)).fixReceived(device, doubleVectorFix);
        Mockito.verifyNoMoreInteractions(listener2);
        Mockito.verifyZeroInteractions(listener1);
    }

    @SuppressWarnings("unchecked")
    private FixReceivedListener<DoubleVectorFix> mockFixReceivedListener() {
        FixReceivedListener<DoubleVectorFix> listener = Mockito.mock(FixReceivedListener.class);
        return listener;
    }
    
    private void verifySingleFix(Timed expectedFix, long start, long end, DeviceIdentifier device, boolean endIsInclusive) throws Exception {
        List<Timed> loadedFixes = loadFixes(start, end, device, endIsInclusive);
        assertEquals(1, loadedFixes.size());
        assertEquals(expectedFix, loadedFixes.get(0));
    }
    
    private List<Timed> loadFixes(long start, long end, DeviceIdentifier device, boolean endIsInclusive)
            throws TransformationException {
        List<Timed> loadedFixes = new ArrayList<>();
        
        store.loadFixes(loadedFixes::add, device, new MillisecondsTimePoint(start), new MillisecondsTimePoint(end), endIsInclusive);
        return loadedFixes;
    }
    
    private void verifyYoungestFix(Timed expectedFix, long start, long end, DeviceIdentifier device) throws Exception {
        assertEquals(expectedFix, loadYoungestFix(start, end, device));
    }
    
    private Timed loadYoungestFix(long start, long end, DeviceIdentifier device)
            throws TransformationException {
        List<Timed> loadedFixes = new ArrayList<>();
        store.loadYoungestFix(loadedFixes::add, device, new TimeRangeImpl(new MillisecondsTimePoint(start), new MillisecondsTimePoint(end)));
        assert(loadedFixes.size() <= 1);
        return loadedFixes.isEmpty() ? null : loadedFixes.get(0);
    }
    
    private void verifyOldestFix(Timed expectedFix, long start, long end, DeviceIdentifier device) throws Exception {
        assertEquals(expectedFix, loadOldestFix(start, end, device));
    }
    
    private Timed loadOldestFix(long start, long end, DeviceIdentifier device)
            throws TransformationException {
        List<Timed> loadedFixes = new ArrayList<>();
        store.loadOldestFix(loadedFixes::add, device, new TimeRangeImpl(new MillisecondsTimePoint(start), new MillisecondsTimePoint(end)));
        assert(loadedFixes.size() <= 1);
        return loadedFixes.isEmpty() ? null : loadedFixes.get(0);
    }

    private DoubleVectorFix addBravoFix(DeviceIdentifier device, long timestamp, double rideHeight) {
        DoubleVectorFix fix = createBravoDoubleVectorFixWithRideHeight(timestamp, rideHeight);
        store.storeFix(device, fix);
        return fix;
    }

    private DoubleVectorFix createBravoDoubleVectorFixWithRideHeight(long timestamp, double rideHeight) {
        Double[] fixData = new Double[BravoSensorDataMetadata.getTrackColumnCount()];
        fixData[BravoSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex()] = rideHeight;
        fixData[BravoSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex()] = rideHeight;
        return new DoubleVectorFixImpl(new MillisecondsTimePoint(timestamp), fixData);
    }
}
