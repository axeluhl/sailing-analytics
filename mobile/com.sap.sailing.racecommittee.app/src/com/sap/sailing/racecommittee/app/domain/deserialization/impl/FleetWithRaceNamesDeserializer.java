package com.sap.sailing.racecommittee.app.domain.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.impl.FleetWithRaceNamesImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class FleetWithRaceNamesDeserializer implements JsonDeserializer<FleetWithRaceNames> {

	private JsonDeserializer<Fleet> fleetDeserializer;
	
	public FleetWithRaceNamesDeserializer(
			JsonDeserializer<Fleet> fleetDeserializer) {
		this.fleetDeserializer = fleetDeserializer;
	}
	public FleetWithRaceNames deserialize(JSONObject object)
			throws JsonDeserializationException {
		Fleet fleet = fleetDeserializer.deserialize(object);
		
		Collection<String> raceNames = new ArrayList<String>();
		for (Object raceName : Helpers.getNestedArraySafe(object, "races")) {
			raceNames.add(raceName.toString());
		}
		
		return new FleetWithRaceNamesImpl(fleet, raceNames);
	}
	
}
