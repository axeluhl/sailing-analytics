package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racecommittee.RaceCommitteeCourseAreaChangedEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCommitteeCourseAreaChangedEventSerializer extends BaseRaceCommitteeEventSerializer {
	
	public static final String VALUE_CLASS = RaceCommitteeCourseAreaChangedEvent.class.getSimpleName();
	public static final String FIELD_COURSE_AREA_ID = "courseAreaId";
	
	public RaceCommitteeCourseAreaChangedEventSerializer(
			JsonSerializer<Competitor> competitorSerializer) {
		super(competitorSerializer);
	}

	@Override
	protected String getClassFieldValue() {
		return VALUE_CLASS;
	}
	
	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		RaceCommitteeCourseAreaChangedEvent caChangedEvent = (RaceCommitteeCourseAreaChangedEvent) object;
		
		JSONObject result = super.serialize(caChangedEvent);
		result.put(FIELD_COURSE_AREA_ID, caChangedEvent.getCourseAreaId());
		
		return result;
	}

}
