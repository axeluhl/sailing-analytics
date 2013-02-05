package com.sap.sailing.server.gateway.serialization.impl.regatta;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaces;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetWithRacesImpl;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FleetWithRacesOfSeriesExtensionSerializer extends ExtensionJsonSerializer<Series, FleetWithRaces> {
	public static final String FIELD_NAME = "fleets";
	
	public FleetWithRacesOfSeriesExtensionSerializer(JsonSerializer<FleetWithRaces> extensionSerializer) {
		super(extensionSerializer);
	}
	
	@Override
	public String getExtensionFieldName() {
		return FIELD_NAME;
	}

	@Override
	public Object serializeExtension(Series object) {
		JSONArray result = new JSONArray();
		
		for (Fleet fleet : object.getFleets()) {
			
			Collection<RaceDefinition> races = new ArrayList<RaceDefinition>();
			for (RaceColumnInSeries raceColumn : object.getRaceColumns()) {
				races.add(raceColumn.getRaceDefinition(fleet));
			}
		
			result.add(serialize(new FleetWithRacesImpl(fleet, races)));
		}
		
		return result;
	}

}
