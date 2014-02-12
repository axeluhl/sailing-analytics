package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.racelog.tracking.impl.GPSFixJsonHandlerImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;

public class MockGPSFixJsonHandler extends GPSFixJsonHandlerImpl<GPSFixMoving> {
	public MockGPSFixJsonHandler() {
		super(new GPSFixMovingJsonDeserializer(), new GPSFixMovingJsonSerializer());
	}

}
