package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;

public class RaceCommitteeEventSerializer implements JsonSerializer<RaceCommitteeEvent> {

	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		JSONObject result = new JSONObject();
		result.put("id", object.getId().toString());
		result.put("timestamp", object.getTimePoint().asMillis());
		result.put("passId", object.getPassId());
		return result;
	}

}
