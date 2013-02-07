package com.sap.sailing.racecommittee.app.domain.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.racecommittee.app.domain.impl.SeriesDataImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.FleetWithRaceNamesOfSeriesExtensionSerializer;

public class SeriesDataDeserializer implements JsonDeserializer<SeriesData> {

	private JsonDeserializer<FleetWithRaceNames> fleetDeserializer;
	
	public SeriesDataDeserializer(JsonDeserializer<FleetWithRaceNames> fleetDeserializer) {
		this.fleetDeserializer = fleetDeserializer;
	}

	public SeriesData deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get("name").toString();
		boolean isMedal = (Boolean) object.get("isMedal");
		
		Collection<FleetWithRaceNames> fleets = new ArrayList<FleetWithRaceNames>();
		for (Object fleetObject : Helpers.getNestedArraySafe(
				object, 
				FleetWithRaceNamesOfSeriesExtensionSerializer.FIELD_FLEETS)) {
			JSONObject fleetJson = Helpers.toJSONObjectSafe(fleetObject);
			fleets.add(fleetDeserializer.deserialize(fleetJson));
		}
		
		return new SeriesDataImpl(name, fleets, isMedal);
	}
	
}