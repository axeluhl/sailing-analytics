package com.sap.sailing.domain.test.mock;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;

public class MockGPSFixJsonSerializationServiceFinder implements TypeBasedServiceFinder<GPSFixJsonSerializationHandler> {
    private final MockGPSFixJsonSerializationHandler handler = new MockGPSFixJsonSerializationHandler();
    
    @Override
    public GPSFixJsonSerializationHandler findService(String fixType) {
        if (fixType.equals(GPSFixMovingJsonDeserializer.TYPE)) {
            return handler;
        }
        return null;
    }
}
