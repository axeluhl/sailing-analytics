package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.RaceDefinition;

public class CourseAreaJsonSerializer implements JsonSerializer<CourseArea>
{

	@Override
	public JSONObject serialize(CourseArea object) {
		JSONObject result = new JSONObject();
		
		result.put("name", object.getName());
		JSONArray races = new JSONArray();
		for (RaceDefinition race : object.getRaces())
		{
			races.add(race.getId().toString());
		}
		result.put("races", races);
		
		return result;
	}
	
}
