package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;

public class RaceDefinitionJsonSerializer implements JsonSerializer<RaceDefinition> {

	@Override
	public JSONObject serialize(RaceDefinition object) {
		JSONObject result = new JSONObject();
		
		result.put("id", object.getId().toString());
		result.put("name", object.getName());
		result.put("course", "tbd");
		
		return result;
	}

}
