package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;

public class MockGPSFixJsonServiceFinder implements TypeBasedServiceFinder<GPSFixJsonHandler> {
    private final MockGPSFixJsonHandler handler = new MockGPSFixJsonHandler();
    
    @Override
    public GPSFixJsonHandler findService(String fixType) {
        if (fixType.equals(GPSFixMovingJsonDeserializer.TYPE)) {
            return handler;
        }
        return null;
    }
}
