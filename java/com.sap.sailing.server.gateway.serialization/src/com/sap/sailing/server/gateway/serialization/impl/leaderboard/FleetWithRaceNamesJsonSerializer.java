package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FleetWithRaceNamesJsonSerializer implements JsonSerializer<FleetWithRaceNames> {
	public static final String FIELD_RACES = "races";

	private final JsonSerializer<Fleet> fleetSerializer;
	
	public FleetWithRaceNamesJsonSerializer(JsonSerializer<Fleet> fleetSerializer) {
		this.fleetSerializer = fleetSerializer;
	}
	
	
	@Override
	public JSONObject serialize(FleetWithRaceNames object) {
		JSONObject result = fleetSerializer.serialize(object);
		
		JSONArray races = new JSONArray();
		for (String raceName : object.getRaceNames()) {
			races.add(raceName);
		}
		result.put(FIELD_RACES, races);
		
		return result;
	}

}
