package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

/**
 * Implements JsonSerializer<RaceCommitteeEvent> to enable a consistent interface when used
 * with {@link RaceCommitteeEventSerializerFactory}, see type-safeness of generics.
 */
public class RaceCommitteeStartTimeEventSerializer implements JsonSerializer<RaceCommitteeEvent> {

	private JsonSerializer<RaceCommitteeEvent> baseSerializer;
	
	public RaceCommitteeStartTimeEventSerializer(JsonSerializer<RaceCommitteeEvent> baseSerializer) {
		this.baseSerializer = baseSerializer;
	}
	
	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		// We have to cast here, see class comment
		RaceCommitteeStartTimeEvent startTimeEvent = (RaceCommitteeStartTimeEvent) object;
		
		JSONObject result = baseSerializer.serialize(startTimeEvent);
		result.put("startTime", startTimeEvent.getStartTime().asMillis());
		return result;
	}

}
