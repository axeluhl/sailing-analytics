package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogStartTimeEventSerializer extends BaseRaceLogEventSerializer {
	public static final String VALUE_CLASS = RaceLogStartTimeEvent.class.getSimpleName();
	public static final String FIELD_START_TIME = "startTime";
	
	@Override
	protected String getClassFieldValue() {
		return VALUE_CLASS;
	}
	
	@Override
	public JSONObject serialize(RaceLogEvent object) {
		RaceLogStartTimeEvent startTimeEvent = (RaceLogStartTimeEvent) object;
		
		JSONObject result = super.serialize(startTimeEvent);
		result.put(FIELD_START_TIME, startTimeEvent.getStartTime().asMillis());
		return result;
	}

}
