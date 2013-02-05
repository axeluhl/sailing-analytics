package com.sap.sailing.racecommittee.app.domain.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;

public class BoatClassJsonDeserializer implements JsonDeserializer<BoatClass> {

	public BoatClass deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(BoatClassJsonSerializer.FIELD_NAME).toString();
		return new BoatClassImpl(name, false);
	}

}
