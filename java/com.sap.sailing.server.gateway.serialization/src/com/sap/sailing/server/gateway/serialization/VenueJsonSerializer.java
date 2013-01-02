package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;


public class VenueJsonSerializer implements JsonSerializer<Venue>
{
	
	private JsonSerializer<CourseArea> areaSerializer;
	
	public VenueJsonSerializer(JsonSerializer<CourseArea> areaSerializer)
	{
		this.areaSerializer = areaSerializer;
	}

	@Override
	public JSONObject serialize(Venue object) {
		JSONObject result = new JSONObject();
		
		result.put("name", object.getName());
		JSONArray areas = new JSONArray();
		for (CourseArea area : object.getCourseAreas())
		{
			areas.add(areaSerializer.serialize(area));
		}
		result.put("courseAreas", areas);
		
		return result;
	}
}
