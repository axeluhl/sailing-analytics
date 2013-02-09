package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceRow;
import com.sap.sailing.racecommittee.app.domain.SeriesWithRows;
import com.sap.sailing.racecommittee.app.domain.impl.SeriesWithRowsImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.RaceRowsOfSeriesDataExtensionSerializer;

public class SeriesWithRowsDeserializer implements JsonDeserializer<SeriesWithRows> {

	private JsonDeserializer<RaceRow> raceRowDeserializer;
	
	public SeriesWithRowsDeserializer(JsonDeserializer<RaceRow> raceRowDeserializer) {
		this.raceRowDeserializer = raceRowDeserializer;
	}

	public SeriesWithRows deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get("name").toString();
		boolean isMedal = (Boolean) object.get("isMedal");
		
		Collection<RaceRow> rows = new ArrayList<RaceRow>();
		for (Object fleetObject : Helpers.getNestedArraySafe(
				object, 
				RaceRowsOfSeriesDataExtensionSerializer.FIELD_FLEETS)) {
			JSONObject fleetJson = Helpers.toJSONObjectSafe(fleetObject);
			rows.add(raceRowDeserializer.deserialize(fleetJson));
		}
		
		return new SeriesWithRowsImpl(name, rows, isMedal);
	}
	
}