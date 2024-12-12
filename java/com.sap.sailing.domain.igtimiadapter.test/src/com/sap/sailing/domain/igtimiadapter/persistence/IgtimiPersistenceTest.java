package com.sap.sailing.domain.igtimiadapter.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class IgtimiPersistenceTest {
    private MongoObjectFactory mongoObjectFactory;
    private DomainObjectFactory domainObjectFactory;
    
    @Before
    public void setUp() {
        MongoDBConfiguration testDBConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(testDBConfig.getService());
        mongoObjectFactory.clear();
        domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(testDBConfig.getService());
    }

    @Test
    public void testStoringAndLoadingSimpleDevice() {
        final Device device = Device.create(123, "DE-AC-AAHJ", "The tricky one", "ABCDE");
        mongoObjectFactory.storeDevice(device);
        final Device loadedDevice = domainObjectFactory.getDevices().iterator().next();
        assertEquals(device.getId(), loadedDevice.getId());
        assertEquals(device.getName(), loadedDevice.getName());
        assertEquals(device.getSerialNumber(), loadedDevice.getSerialNumber());
        assertEquals(device.getServiceTag(), loadedDevice.getServiceTag());
    }

    @Test
    public void testStoringAndLoadingSimpleResource() {
        final TimePoint from = TimePoint.now();
        final TimePoint to = from.plus(Duration.ONE_MINUTE);
        final Resource resource = Resource.create(234, from, to, "AA-DE-AACC", new int[] { 1, 4, 7 });
        mongoObjectFactory.storeResource(resource);
        final Resource loadedResource = domainObjectFactory.getResources().iterator().next();
        assertEquals(resource.getId(), loadedResource.getId());
        assertEquals(resource.getName(), loadedResource.getName()); // an inferred value; still useful to compare
        assertEquals(resource.getStartTime(), loadedResource.getStartTime());
        assertEquals(resource.getEndTime(), loadedResource.getEndTime());
        assertEquals(resource.getDeviceSerialNumber(), loadedResource.getDeviceSerialNumber());
        assertEquals(Util.asSet(resource.getDataTypes()), Util.asSet(loadedResource.getDataTypes()));
    }

    @Test
    public void testStoringAndLoadingSimpleDataAccessWindow() {
        final TimePoint from = TimePoint.now();
        final TimePoint to = from.plus(Duration.ONE_MINUTE);
        final DataAccessWindow daw = DataAccessWindow.create(345, from, to, "AA-CC-AAEH");
        mongoObjectFactory.storeDataAccessWindow(daw);
        final DataAccessWindow loadedDaw = domainObjectFactory.getDataAccessWindows().iterator().next();
        assertEquals(daw.getId(), loadedDaw.getId());
        assertEquals(daw.getName(), loadedDaw.getName()); // an inferred value; still useful to compare
        assertEquals(daw.getStartTime(), loadedDaw.getStartTime());
        assertEquals(daw.getEndTime(), loadedDaw.getEndTime());
        assertEquals(daw.getDeviceSerialNumber(), loadedDaw.getDeviceSerialNumber());
    }
}
