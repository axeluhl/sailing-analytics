package com.sap.sailing.racecommittee.app.domain.deserialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.racecommittee.app.domain.impl.SimpleRegattaImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;

public class RegattaJsonDeserializer implements JsonDeserializer<Regatta> {

	private JsonDeserializer<BoatClass> boatClassDeserializer;
	private JsonDeserializer<RaceDefinition> raceDefinitionDeserializer;
	
	public RegattaJsonDeserializer(
			JsonDeserializer<BoatClass> boatClassDeserializer,
			JsonDeserializer<RaceDefinition> raceDefinitionDeserializer) {
		this.raceDefinitionDeserializer = raceDefinitionDeserializer;
		this.boatClassDeserializer = boatClassDeserializer;
	}
	
	public Regatta deserialize(JSONObject object) throws JsonDeserializationException {
		String id = object.get(RegattaJsonSerializer.FIELD_ID).toString();
		//String name = object.get(RegattaJsonSerializer.FIELD_NAME).toString();
		String baseName = object.get(RegattaJsonSerializer.FIELD_BASE_NAME).toString();
		BoatClass boatClass = boatClassDeserializer.deserialize(
				Helpers.getNestedObjectSafe(object, RegattaJsonSerializer.FIELD_BOAT_CLASS));
		
		Regatta regatta = new SimpleRegattaImpl(Helpers.tryUuidConversion(id), baseName, boatClass);
		
		JSONArray races = Helpers.getNestedArraySafe(
				object, 
				RegattaJsonSerializer.FIELD_RACES);
		for (Object element : races) {
			JSONObject race = Helpers.toJSONObjectSafe(element);
			regatta.addRace(raceDefinitionDeserializer.deserialize(race));
		}
		
		return regatta;
	}

}
