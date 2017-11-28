package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockEmptyServiceFinder;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiJsonHandler;
import com.sap.sailing.domain.racelogtracking.PlaceHolderDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.PlaceHolderDeviceIdentifierJsonHandler;
import com.sap.sse.common.TypeBasedServiceFinder;

public class DeviceIdentifierSerializationTest {
    @Before
    public void setup() {
    }
    
    @Test
    public void testSerializationOfDeviceIdentifier() throws JsonDeserializationException {
        final DeviceIdentifierJsonDeserializer myDeserializer = DeviceIdentifierJsonDeserializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE);
        final DeviceIdentifierJsonSerializer mySerializer = DeviceIdentifierJsonSerializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE);
        DeviceIdentifier device = new SmartphoneImeiIdentifier("abc");
        JSONObject json = mySerializer.serialize(device);
        DeviceIdentifier deserialized = myDeserializer.deserialize(json);
        assertEquals(device, deserialized);
    }
    
    @Test
    public void testSerializationOfDeviceIdentifierWithNoSerializer() throws JsonDeserializationException {
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> onlyFallback = new MockEmptyServiceFinder<>();
        onlyFallback.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
        final DeviceIdentifierJsonDeserializer myDeserializer = new DeviceIdentifierJsonDeserializer(onlyFallback);
        final DeviceIdentifierJsonSerializer mySerializer = DeviceIdentifierJsonSerializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE);
        DeviceIdentifier device = new SmartphoneImeiIdentifier("abc");
        JSONObject json = mySerializer.serialize(device);
        DeviceIdentifier deserialized = myDeserializer.deserialize(json);
        assertTrue(deserialized instanceof PlaceHolderDeviceIdentifier);
        assertEquals(device.getStringRepresentation(), deserialized.getStringRepresentation());
        assertEquals(device.getIdentifierType(), deserialized.getIdentifierType());
    }
    
    @Test
    public void testSerializationOfDeviceIdentifierWithNoDeserializer() throws JsonDeserializationException {
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> onlyFallback = new MockEmptyServiceFinder<>();
        onlyFallback.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
        final DeviceIdentifierJsonDeserializer myDeserializer = DeviceIdentifierJsonDeserializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE);
        final DeviceIdentifierJsonSerializer mySerializer = new DeviceIdentifierJsonSerializer(onlyFallback);
        //track file device id can't be restored from string rep
        DeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl("file", "track");
        JSONObject json = mySerializer.serialize(device);
        DeviceIdentifier deserialized = myDeserializer.deserialize(json);
        assertTrue(deserialized instanceof PlaceHolderDeviceIdentifier);
        assertEquals(device.getStringRepresentation(), deserialized.getStringRepresentation());
        assertEquals(device.getIdentifierType(), deserialized.getIdentifierType());
    }
}
