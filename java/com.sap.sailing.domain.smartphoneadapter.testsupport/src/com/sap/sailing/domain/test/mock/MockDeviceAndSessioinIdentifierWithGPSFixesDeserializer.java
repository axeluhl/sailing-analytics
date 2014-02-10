package com.sap.sailing.domain.test.mock;

import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;

public class MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer extends
		DeviceAndSessionIdentifierWithGPSFixesDeserializer {

	public MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer() {
		super(new MockGPSFixJsonSerializationServiceFinder(),
				new MockDeviceTypeServiceFinder<DeviceIdentifierJsonSerializationHandler>());
	}

}
