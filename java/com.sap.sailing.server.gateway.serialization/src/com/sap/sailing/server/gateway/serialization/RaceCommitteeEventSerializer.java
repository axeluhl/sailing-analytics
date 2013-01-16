package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;

public class RaceCommitteeEventSerializer implements JsonSerializer<RaceCommitteeEvent> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_TIMESTAMP = "timestamp";
	public static final String FIELD_PASS_ID = "passId";
	
	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		JSONObject result = new JSONObject();
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_TIMESTAMP, object.getTimePoint().asMillis());
		result.put(FIELD_PASS_ID, object.getPassId());
		return result;
	}

}
