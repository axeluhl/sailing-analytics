package com.sap.sailing.server.gateway.serialization.devices;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.SmartphoneImeiIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.SmartphoneImeiIdentifierJsonSerializer;

public class SmartphoneImeiJsonSerializationHandler implements DeviceIdentifierJsonSerializationHandler {
	private final SmartphoneImeiIdentifierJsonSerializer serializer = new SmartphoneImeiIdentifierJsonSerializer();
	private final SmartphoneImeiIdentifierJsonDeserializer deserializer = new SmartphoneImeiIdentifierJsonDeserializer();

	@Override
	public JSONObject serialize(DeviceIdentifier object)
			throws IllegalArgumentException {
		return serializer.serialize((SmartphoneImeiIdentifier) object);
	}

	@Override
	public DeviceIdentifier deserialize(JSONObject json)
			throws JsonDeserializationException {
		return deserializer.deserialize(json);
	}

}
