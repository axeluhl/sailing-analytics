package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class RaceLogsDataParser implements
		DataParser<Map<ManagedRace, Collection<RaceLogEvent>>> {

	public Map<ManagedRace, Collection<RaceLogEvent>> parse(Reader reader)
			throws Exception {
		Object parsedResult = JSONValue.parse(reader);
		System.out.println(parsedResult);
		return new HashMap<ManagedRace, Collection<RaceLogEvent>>();
	}

}
