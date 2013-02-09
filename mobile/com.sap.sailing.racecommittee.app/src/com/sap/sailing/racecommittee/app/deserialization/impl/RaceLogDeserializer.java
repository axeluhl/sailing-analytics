package com.sap.sailing.racecommittee.app.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class RaceLogDeserializer implements JsonDeserializer<RaceLog> {

	public RaceLog deserialize(JSONObject object)
			throws JsonDeserializationException {
		// TODO Deserialize race log
		return null;
	}

}
