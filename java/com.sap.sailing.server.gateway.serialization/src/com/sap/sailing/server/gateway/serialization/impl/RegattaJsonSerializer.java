package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RegattaJsonSerializer implements JsonSerializer<Regatta> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_BASE_NAME = "baseName";
	public static final String FIELD_BOAT_CLASS = "boatClass";
	public static final String FIELD_RACES = "races";
	
	private JsonSerializer<BoatClass> boatClassSerializer;
	private JsonSerializer<RaceDefinition> raceDefinitionSerializer;
	
	public RegattaJsonSerializer(
			JsonSerializer<BoatClass> boatClassSerializer,
			JsonSerializer<RaceDefinition> raceDefinitionSerializer)
	{
		this.boatClassSerializer = boatClassSerializer;
		this.raceDefinitionSerializer = raceDefinitionSerializer;
	}
	
	@Override
	public JSONObject serialize(Regatta object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_BASE_NAME, object.getBaseName());
		result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(object.getBoatClass()));
		
		JSONArray races = new JSONArray();
		for (RaceDefinition race : object.getAllRaces())
		{
			races.add(raceDefinitionSerializer.serialize(race));
		}
		result.put(FIELD_RACES, races);
		
		return result;
	}

}
