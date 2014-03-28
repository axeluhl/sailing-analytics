package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;

public class MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer extends
DeviceAndSessionIdentifierWithGPSFixesDeserializer {

    public MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer() {
        super(new MockGPSFixJsonServiceFinder(),
                DeviceIdentifierJsonDeserializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE));
    }

}
