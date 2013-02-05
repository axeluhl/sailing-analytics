package com.sap.sailing.server.gateway.serialization.impl.race;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseOfRaceDefinitionExtensionSerializer extends
		ExtensionJsonSerializer<RaceDefinition, Course> {
	public static final String FIELD_COURSE = "COURSE";

	public CourseOfRaceDefinitionExtensionSerializer(
			JsonSerializer<Course> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	public String getExtensionFieldName() {
		return FIELD_COURSE;
	}

	@Override
	public Object serializeExtension(RaceDefinition parent) {
		return extensionSerializer.serialize(parent.getCourse());
	}

}
