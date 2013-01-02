package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;

public class RegattaJsonSerializer implements JsonSerializer<Regatta> {

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
		
		result.put("id", object.getId().toString());
		result.put("baseName", object.getBaseName());
		result.put("boatClass", boatClassSerializer.serialize(object.getBoatClass()));
		
		JSONArray races = new JSONArray();
		for (RaceDefinition race : object.getAllRaces())
		{
			races.add(raceDefinitionSerializer.serialize(race));
		}
		result.put("races", races);
		
		return result;
	}

}
