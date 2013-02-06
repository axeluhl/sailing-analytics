package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.FleetWithRaceNames;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.impl.RaceIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class ManagedRacesDataParser implements DataParser<Collection<ManagedRace>> {
	private static final String TAG = ManagedRacesDataParser.class.getName();
	
	
	private JsonDeserializer<RaceGroup> deserializer;
	
	public ManagedRacesDataParser(JsonDeserializer<RaceGroup> deserializer) {
		this.deserializer = deserializer;
	}
	
	public Collection<ManagedRace> parse(Reader reader) throws Exception {
		Object parsedResult = JSONValue.parse(reader);
		JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
		
		Collection<ManagedRace> managedRaces = new ArrayList<ManagedRace>();
		for (Object element : jsonArray) {
			JSONObject json = Helpers.toJSONObjectSafe(element);
			
			RaceGroup group = deserializer.deserialize(json);
			addManagedRaces(managedRaces, group);
		}
		
		return managedRaces;
	}

	private void addManagedRaces(Collection<ManagedRace> target, RaceGroup raceGroup) {
		for (SeriesData series : raceGroup.getSeries()) {
			for (Fleet fleet : series.getFleets()) {
				if (fleet instanceof FleetWithRaceNames) {
					FleetWithRaceNames fleetWithRaceNames = (FleetWithRaceNames) fleet;
					for (String raceName : fleetWithRaceNames.getRaceNames()) {
						ManagedRace managedRace = createManagedRace(
								raceGroup,
								series, 
								fleetWithRaceNames, 
								raceName);
						target.add(managedRace);
					}
				} else {
					ExLog.w(TAG, String.format("Fleet %s had no race information attached.", fleet));
				}
			}
		}
		
	}

	private ManagedRace createManagedRace(
			RaceGroup raceGroup,
			SeriesData series, 
			FleetWithRaceNames fleetWithRaceNames,
			String raceName) {
		ManagedRaceIdentifier identifier = 
				new RaceIdentifierImpl(raceName, fleetWithRaceNames, series, raceGroup);
		ManagedRace managedRace = new ManagedRaceImpl(identifier);
		return managedRace;
	}

}
