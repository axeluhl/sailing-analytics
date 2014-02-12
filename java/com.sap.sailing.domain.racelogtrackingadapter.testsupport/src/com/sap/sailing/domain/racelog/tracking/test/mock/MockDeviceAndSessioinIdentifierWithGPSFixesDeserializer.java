package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;

public class MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer extends
		DeviceAndSessionIdentifierWithGPSFixesDeserializer {

	public MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer() {
		super(new MockGPSFixJsonServiceFinder(),
				new MockSmartphoneImeiJsonServiceFinder());
	}

}
