package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetWithRaceNamesImpl;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FleetWithRaceNamesOfSeriesExtensionSerializer extends ExtensionJsonSerializer<Series, FleetWithRaceNames> {

	public FleetWithRaceNamesOfSeriesExtensionSerializer(
			JsonSerializer<FleetWithRaceNames> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	public String getExtensionFieldName() {
		return "fleets";
	}

	@Override
	public Object serializeExtension(Series parent) {
		JSONArray result = new JSONArray();
		
		for (Fleet fleet : parent.getFleets()) {
			Collection<String> raceNames = new ArrayList<String>();
			for (RaceColumn column : parent.getRaceColumns()) {
				raceNames.add(column.getName());
			}
			result.add(serialize(new FleetWithRaceNamesImpl(fleet, raceNames)));
		}
		
		return result;
	}

}
