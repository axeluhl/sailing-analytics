package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeCourseAreaChangeEventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeCourseAreaChangedEventSerializer;

public class RaceCommitteeCourseAreaChangedEventDeserializer extends
		BaseRaceCommitteeEventDeserializer {

	@Override
	protected RaceCommitteeEvent deserialize(JSONObject object, UUID id,
			TimePoint timePoint, int passId) throws JsonDeserializationException {
		
		String courseAreaId = object.get(RaceCommitteeCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID).toString(); 
		UUID courseAreaUuid = null;
		try {
			courseAreaUuid = UUID.fromString(courseAreaId);
		} catch (IllegalArgumentException iae) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s couldn't be parsed as UUID.", RaceCommitteeCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID, id), 
					iae);
		}
		
		return new RaceCommitteeCourseAreaChangeEventImpl(timePoint, id, Collections.<Competitor>emptyList(), passId, courseAreaUuid);
	}

}
