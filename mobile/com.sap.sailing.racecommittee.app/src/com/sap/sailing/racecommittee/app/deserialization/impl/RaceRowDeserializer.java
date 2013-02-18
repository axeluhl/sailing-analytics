package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.base.RaceRow;
import com.sap.sailing.domain.base.impl.RaceRowImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceRowJsonSerializer;

public class RaceRowDeserializer implements JsonDeserializer<RaceRow> {

	private JsonDeserializer<Fleet> fleetDeserializer;
	private JsonDeserializer<RaceCell> raceCellDeserializer;
	
	public RaceRowDeserializer(JsonDeserializer<Fleet> fleetDeserializer,
			JsonDeserializer<RaceCell> raceCellDeserializer) {
		this.fleetDeserializer = fleetDeserializer;
		this.raceCellDeserializer = raceCellDeserializer;
	}

	public RaceRow deserialize(JSONObject object)
			throws JsonDeserializationException {
		JSONObject fleetObject = Helpers.toJSONObjectSafe(object.get(RaceRowJsonSerializer.FIELD_FLEET));
		JSONArray cellsObject = Helpers.getNestedArraySafe(object, RaceRowJsonSerializer.FIELD_RACE_CELLS);
		
		Collection<RaceCell> races = new ArrayList<RaceCell>();
		Fleet fleet = fleetDeserializer.deserialize(fleetObject);
		for (Object cellObject : cellsObject) {
			JSONObject cellJson = Helpers.toJSONObjectSafe(cellObject);
			races.add(raceCellDeserializer.deserialize(cellJson));
		}
		
		return new RaceRowImpl(fleet, races);
	}

}
