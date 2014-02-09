package com.sap.sailing.domain.test.mock;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;

public class MockGPSFixJsonSerializationHandler implements
		GPSFixJsonSerializationHandler {

	@Override
	public JSONObject serialize(GPSFix object) throws IllegalArgumentException {
		if (!(object instanceof GPSFixMoving)) throw new IllegalArgumentException("Only serializes GPSFixMoving");
		return new GPSFixMovingJsonSerializer().serialize((GPSFixMoving) object);
	}

	@Override
	public GPSFix deserialize(JSONObject json)
			throws JsonDeserializationException {
		return new GPSFixMovingJsonDeserializer().deserialize(json);
	}

}
