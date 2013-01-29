package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceDefinitionJsonSerializer implements JsonSerializer<RaceDefinition> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_COURSE = "course";
	
	private final JsonSerializer<Course> courseSerializer;
	
	public RaceDefinitionJsonSerializer(JsonSerializer<Course> courseSerializer) {
		this.courseSerializer = courseSerializer;
	}

	@Override
	public JSONObject serialize(RaceDefinition object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_COURSE, courseSerializer.serialize(object.getCourse()));
		
		return result;
	}

}
