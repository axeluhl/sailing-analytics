package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;

public class BoatClassJsonSerializer implements JsonSerializer<BoatClass> {

	@Override
	public JSONObject serialize(BoatClass object) {
		JSONObject result = new JSONObject();
		result.put("name", object.getName());
		return result;
	}

}
