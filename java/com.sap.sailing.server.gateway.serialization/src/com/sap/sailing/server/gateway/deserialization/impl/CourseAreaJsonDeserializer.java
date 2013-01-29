package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;

public class CourseAreaJsonDeserializer implements JsonDeserializer<CourseArea> {

	@Override
	public CourseArea deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(CourseAreaJsonSerializer.FIELD_NAME).toString();
		
		CourseArea courseArea = new CourseAreaImpl(name);
		
		/// TODO: link races to courseArea...
		/*JSONArray racesArray = Helpers.getNestedArraySafe(object, CourseAreaJsonSerializer.FIELD_RACES);
		for (Object element : racesArray) {
			System.out.println(element.toString());
		}*/
		
		return courseArea;
	}

}
