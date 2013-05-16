package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;

public class CourseAreaJsonDeserializer implements JsonDeserializer<CourseArea> {

	public CourseArea deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(CourseAreaJsonSerializer.FIELD_NAME).toString();
		Serializable id = (Serializable) object.get(CourseAreaJsonSerializer.FIELD_ID);
		
		return new CourseAreaImpl(name, Helpers.tryUuidConversion(id));
	}

}
