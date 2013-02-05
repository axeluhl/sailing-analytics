package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogCourseAreaChangeEventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogCourseAreaChangedEventSerializer;

public class RaceLogCourseAreaChangedEventDeserializer extends
		BaseRaceLogEventDeserializer {

	@Override
	protected RaceLogEvent deserialize(JSONObject object, Serializable id,
			TimePoint timePoint, int passId) throws JsonDeserializationException {
		
		String courseAreaId = object.get(RaceLogCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID).toString(); 
		UUID courseAreaUuid = null;
		try {
			courseAreaUuid = UUID.fromString(courseAreaId);
		} catch (IllegalArgumentException iae) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s couldn't be parsed as UUID.", RaceLogCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID, id), 
					iae);
		}
		
		return new RaceLogCourseAreaChangeEventImpl(timePoint, id, Collections.<Competitor>emptyList(), passId, courseAreaUuid);
	}

}
