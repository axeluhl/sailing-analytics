package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.impl.PassAwareRaceLogImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class ManagedRacesDataParser implements DataParser<Collection<ManagedRace>> {
	// private static final String TAG = ManagedRacesDataParser.class.getName();
	
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
		for (SeriesWithRows series : raceGroup.getSeries()) {
			for (RaceRow raceRow : series.getRaceRows()) {
				Fleet fleet = raceRow.getFleet();
				for (RaceCell cell : raceRow.getCells()) {
					PassAwareRaceLog log = cell.getRaceLog() instanceof PassAwareRaceLog ? 
							(PassAwareRaceLog) cell.getRaceLog() : 
								PassAwareRaceLogImpl.copy(cell.getRaceLog());
					ManagedRace race = createManagedRace(
							raceGroup, 
							series, 
							fleet, 
							cell.getName(), 
							log);
					target.add(race);
				}
			}
		}
	}

	private ManagedRace createManagedRace(RaceGroup raceGroup, SeriesWithRows series,
			Fleet fleet, String name, PassAwareRaceLog raceLog) {
		return new ManagedRaceImpl(
				new ManagedRaceIdentifierImpl(name, 
						new FleetIdentifierImpl(fleet, series, raceGroup)),
				StartProcedureType.ESS,
				raceLog);
		
	}

}
