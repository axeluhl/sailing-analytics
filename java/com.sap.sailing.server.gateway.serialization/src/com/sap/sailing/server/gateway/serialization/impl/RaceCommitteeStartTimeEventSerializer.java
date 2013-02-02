package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCommitteeStartTimeEventSerializer extends BaseRaceCommitteeEventSerializer {

	public static final String VALUE_CLASS = RaceCommitteeStartTimeEvent.class.getSimpleName();
	public static final String FIELD_START_TIME = "startTime";
	
	public RaceCommitteeStartTimeEventSerializer(
			JsonSerializer<Competitor> competitorSerializer) {
		super(competitorSerializer);
	}
	
	@Override
	protected String getClassFieldValue() {
		return VALUE_CLASS;
	}
	
	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		RaceCommitteeStartTimeEvent startTimeEvent = (RaceCommitteeStartTimeEvent) object;
		
		JSONObject result = super.serialize(startTimeEvent);
		result.put(FIELD_START_TIME, startTimeEvent.getStartTime().asMillis());
		return result;
	}

}
