package com.sap.sailing.domain.igtimiadapter.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.igtimi.IgtimiData.ApparentWindSpeed;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.DataPoint.DataCase;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class IgtimiPersistenceTest {
    private static final Logger logger = Logger.getLogger(IgtimiPersistenceTest.class.getName());

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
        final Device device = Device.create(123, "DE-AC-AAHJ", "The tricky one");
        mongoObjectFactory.storeDevice(device);
        final Device loadedDevice = domainObjectFactory.getDevices().iterator().next();
        assertEquals(device.getId(), loadedDevice.getId());
        assertEquals(device.getName(), loadedDevice.getName());
        assertEquals(device.getSerialNumber(), loadedDevice.getSerialNumber());
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
    
    @Test
    public void testStoringAndLoadingSimpleProtobufMessage() {
        final TimePoint AWS_TIME_POINT = TimePoint.now();
        final String SERIAL_NUMBER = "DE-AA-AAHE";
        final int STREAM_ID = 358;
        final int AWS = 12;
        final Msg msg = Msg.newBuilder().setData(
                Data.newBuilder().addData(
                        DataMsg.newBuilder()
                            .setSerialNumber(SERIAL_NUMBER)
                            .setStreamId(STREAM_ID)
                            .addData(
                                    DataPoint.newBuilder()
                                        .setAws(ApparentWindSpeed.newBuilder()
                                                    .setTimestamp(AWS_TIME_POINT.asMillis())
                                                    .setValue(AWS)
                                                    .build())
                                        .build())
                            .build())
                    .build())
                .build();
        logger.info("Storing message: "+msg);
        mongoObjectFactory.storeMessage(SERIAL_NUMBER, msg);
        final Set<DataCase> dataCases = new HashSet<>(Arrays.asList(DataCase.values()));
        final Iterable<Msg> result = domainObjectFactory.getMessages(SERIAL_NUMBER, TimeRange.create(AWS_TIME_POINT.minus(Duration.ONE_MINUTE), AWS_TIME_POINT.plus(Duration.ONE_MINUTE)), dataCases);
        final Iterator<Msg> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        final Msg readMsg = iterator.next();
        assertFalse(iterator.hasNext());
        logger.info("Read message: "+readMsg);
        final Data data = readMsg.getData();
        final DataMsg dataMessage = data.getData(0);
        assertEquals(SERIAL_NUMBER, dataMessage.getSerialNumber());
        assertEquals(STREAM_ID, dataMessage.getStreamId());
        final DataPoint dataPoint = dataMessage.getData(0);
        final ApparentWindSpeed aws = dataPoint.getAws();
        assertEquals(AWS, aws.getValue(), 0.000000001);
        // search with a time range *before* the actual message
        assertTrue(Util.isEmpty(domainObjectFactory.getMessages(SERIAL_NUMBER, TimeRange.create(AWS_TIME_POINT.minus(Duration.ONE_MINUTE.times(2)), AWS_TIME_POINT.minus(Duration.ONE_MINUTE)), dataCases)));
        // search with a time range *after* the actual message
        assertTrue(Util.isEmpty(domainObjectFactory.getMessages(SERIAL_NUMBER, TimeRange.create(AWS_TIME_POINT.plus(Duration.ONE_MINUTE), AWS_TIME_POINT.plus(Duration.ONE_MINUTE.times(2))), dataCases)));
        // search with the wrong device serial number
        assertTrue(Util.isEmpty(domainObjectFactory.getMessages(SERIAL_NUMBER+"-wrong", TimeRange.create(AWS_TIME_POINT.minus(Duration.ONE_MINUTE), AWS_TIME_POINT.plus(Duration.ONE_MINUTE)), dataCases)));
        // search with a time range open at the end
        assertFalse(Util.isEmpty(domainObjectFactory.getMessages(SERIAL_NUMBER, TimeRange.create(AWS_TIME_POINT.minus(Duration.ONE_MINUTE), null), dataCases)));
        // search with a time range open at the start
        assertFalse(Util.isEmpty(domainObjectFactory.getMessages(SERIAL_NUMBER, TimeRange.create(null, AWS_TIME_POINT.plus(Duration.ONE_MINUTE)), dataCases)));
    }
}
