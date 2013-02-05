package com.sap.sailing.racecommittee.app.domain.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class FleetDeserializer implements JsonDeserializer<Fleet> {

	private JsonDeserializer<Color> colorDeserializer;
	
	public FleetDeserializer(JsonDeserializer<Color> colorDeserializer) {
		this.colorDeserializer = colorDeserializer;
	}

	public Fleet deserialize(JSONObject object)
			throws JsonDeserializationException {
		Color color = null;
		if (object.containsKey("color")) {
			color = colorDeserializer.deserialize(Helpers.getNestedObjectSafe(object, "color"));
		}
		String name = object.get("name").toString();
		Number ordering = (Number) object.get("ordering");
		
		return new FleetImpl(name, ordering.intValue(), color);
	}
	
}
