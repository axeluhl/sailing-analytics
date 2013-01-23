package com.sap.sailing.server.gateway.deserialization;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.serialization.EventJsonSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeEventSerializer;

/// TODO deserialize involved boats
public abstract class RaceCommitteeEventDeserializer implements JsonDeserializer<RaceCommitteeEvent> {

	protected abstract RaceCommitteeEvent deserialize(JSONObject object, UUID id, TimePoint timePoint, int passId)
			throws JsonDeserializationException;
	
	@Override
	public RaceCommitteeEvent deserialize(JSONObject object)
			throws JsonDeserializationException {
		// Factory handles class field and subclassing...
		String id = object.get(RaceCommitteeEventSerializer.FIELD_ID).toString();
		long timeStamp = (Long) object.get(RaceCommitteeEventSerializer.FIELD_TIMESTAMP);
		int passId = (Integer) object.get(RaceCommitteeEventSerializer.FIELD_PASS_ID);
		
		UUID uuid = null;
		try {
			uuid = UUID.fromString(id);
		} catch (IllegalArgumentException iae) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s couldn't be parsed as UUID.", EventJsonSerializer.FIELD_ID, id), 
					iae);
		}
		
		return deserialize(object, uuid, new MillisecondsTimePoint(timeStamp), passId);
	}

}
