package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.SmartphoneImeiIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.SmartphoneImeiIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class SmartphoneImeiJsonHandlerImpl implements DeviceIdentifierJsonHandler {
	private final SmartphoneImeiIdentifierJsonSerializer serializer = new SmartphoneImeiIdentifierJsonSerializer();
	private final SmartphoneImeiIdentifierJsonDeserializer deserializer = new SmartphoneImeiIdentifierJsonDeserializer();

	@Override
	public JSONObject transformForth(DeviceIdentifier object) {
		return serializer.serialize((SmartphoneImeiIdentifier) object);
	}

	@Override
	public DeviceIdentifier transformBack(JSONObject json) throws JsonDeserializationException {
		return deserializer.deserialize(json);
	}

}
