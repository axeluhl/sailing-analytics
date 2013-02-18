package com.sap.sailing.server.gateway.serialization.impl.racegroup;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCellJsonSerializer implements JsonSerializer<RaceCell> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_RACE_LOG = "raceLog";
	
	private JsonSerializer<RaceLog> logSerializer;
	
	public RaceCellJsonSerializer(JsonSerializer<RaceLog> logSerializer) {
		this.logSerializer = logSerializer;
	}

	@Override
	public JSONObject serialize(RaceCell object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_RACE_LOG, logSerializer.serialize(object.getRaceLog()));
		
		return result;
	}

}
