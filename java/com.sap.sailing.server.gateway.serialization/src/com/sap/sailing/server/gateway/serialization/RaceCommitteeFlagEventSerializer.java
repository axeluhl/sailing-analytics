package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;

/**
 * Implements JsonSerializer<RaceCommitteeEvent> to enable a consistent interface when used
 * with {@link RaceCommitteeEventSerializerFactory}, see type-safeness of generics.
 */
public class RaceCommitteeFlagEventSerializer implements JsonSerializer<RaceCommitteeEvent> {

	private JsonSerializer<RaceCommitteeEvent> baseSerializer;
	
	public RaceCommitteeFlagEventSerializer(JsonSerializer<RaceCommitteeEvent> baseSerializer) {
		this.baseSerializer = baseSerializer;
	}
	
	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		// We have to cast here, see class comment
		RaceCommitteeFlagEvent flagEvent = (RaceCommitteeFlagEvent) object;
		
		JSONObject result = baseSerializer.serialize(flagEvent);
		result.put("upperFlag", flagEvent.getUpperFlag());
		result.put("lowerFlag", flagEvent.getLowerFlag());
		result.put("displayed", flagEvent.isDisplayed());
		return result;
	}

}
