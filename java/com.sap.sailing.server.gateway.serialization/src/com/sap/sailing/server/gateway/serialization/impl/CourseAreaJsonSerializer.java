package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseAreaJsonSerializer implements JsonSerializer<CourseArea> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_RACES = "races";

	@Override
	public JSONObject serialize(CourseArea object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		
		return result;
	}
	
}
