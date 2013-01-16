package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;

public class RaceDefinitionJsonSerializer implements JsonSerializer<RaceDefinition> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_COURSE = "course";
	
	@Override
	public JSONObject serialize(RaceDefinition object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_COURSE, "tbd");
		
		return result;
	}

}
