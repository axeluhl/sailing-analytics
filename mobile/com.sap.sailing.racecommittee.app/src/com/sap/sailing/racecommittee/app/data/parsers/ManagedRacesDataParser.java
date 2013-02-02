package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;
import com.sap.sailing.racecommittee.app.domain.impl.SeriesDataImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class ManagedRacesDataParser implements DataParser<Collection<ManagedRace>> {

	private JsonDeserializer<Regatta> regattaDeserializer;
	
	public ManagedRacesDataParser(JsonDeserializer<Regatta> regattaDeserializer) {
		this.regattaDeserializer = regattaDeserializer;
	}
	
	public Collection<ManagedRace> parse(Reader reader) throws Exception {
		Object parsedResult = JSONValue.parse(reader);
		JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
		
		Collection<ManagedRace> managedRaces = new ArrayList<ManagedRace>();
		
		for (Object element : jsonArray) {
			JSONObject json = Helpers.toJSONObjectSafe(element);
			Regatta regatta = regattaDeserializer.deserialize(json);
			
			if (regatta.getAllRaces().iterator().hasNext()) {
				addManagedRaces(managedRaces, regatta);
			}
		}
		
		return managedRaces;
	}

	private void addManagedRaces(Collection<ManagedRace> target, Regatta regatta) {
		/// TODO: parse Series and Fleet information
		for (RaceDefinition raceDefinition : regatta.getAllRaces()) {
			target.add(new ManagedRaceImpl(
					raceDefinition,
					regatta,
					raceDefinition.getBoatClass(),
					new SeriesDataImpl("Unknown", Collections.<Fleet>emptyList(), false),
					new FleetImpl("Unknown")));
		}
	}

}
