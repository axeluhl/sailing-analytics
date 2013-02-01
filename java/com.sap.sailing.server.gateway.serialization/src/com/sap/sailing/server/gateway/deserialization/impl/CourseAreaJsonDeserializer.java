package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;

public class CourseAreaJsonDeserializer implements JsonDeserializer<CourseArea> {

	@Override
	public CourseArea deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(CourseAreaJsonSerializer.FIELD_NAME).toString();
		String id = object.get(CourseAreaJsonSerializer.FIELD_ID).toString();
		
		UUID uuid = null;
		try {
			uuid = UUID.fromString(id);
		} catch (IllegalArgumentException iae) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s couldn't be parsed as UUID.", EventJsonSerializer.FIELD_ID, id), 
					iae);
		}
		
		CourseArea courseArea = new CourseAreaImpl(name, uuid);
		
		/// TODO: link races to courseArea...
		/*JSONArray racesArray = Helpers.getNestedArraySafe(object, CourseAreaJsonSerializer.FIELD_RACES);
		for (Object element : racesArray) {
			System.out.println(element.toString());
		}*/
		
		return courseArea;
	}

}
