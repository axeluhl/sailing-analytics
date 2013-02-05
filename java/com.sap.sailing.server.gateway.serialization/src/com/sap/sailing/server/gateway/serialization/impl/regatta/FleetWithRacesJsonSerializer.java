package com.sap.sailing.server.gateway.serialization.impl.regatta;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaces;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.filter.Filter;

public class FleetWithRacesJsonSerializer implements JsonSerializer<FleetWithRaces> {
	public static final String FIELD_RACES = "races";

	private final JsonSerializer<Fleet> fleetSerializer;
	private final Filter<RaceDefinition> raceDefinitionFilter;
	private final JsonSerializer<RaceDefinition> raceDefinitionSerializer;
	
	public FleetWithRacesJsonSerializer(
			JsonSerializer<Fleet> fleetSerializer,
			Filter<RaceDefinition> raceDefinitionFilter,
			JsonSerializer<RaceDefinition> raceDefinitionSerializer) {
		this.fleetSerializer = fleetSerializer;
		this.raceDefinitionFilter = raceDefinitionFilter;
		this.raceDefinitionSerializer = raceDefinitionSerializer;
	}
	
	
	@Override
	public JSONObject serialize(FleetWithRaces object) {
		JSONObject result = fleetSerializer.serialize(object);
		
		JSONArray races = new JSONArray();
		for (RaceDefinition race : object.getRaceDefinitions()) {
			if (raceDefinitionFilter.isFiltered(race)) {
				races.add(raceDefinitionSerializer.serialize(race));
			}
		}
		result.put(FIELD_RACES, races);
		
		return result;
	}

}
