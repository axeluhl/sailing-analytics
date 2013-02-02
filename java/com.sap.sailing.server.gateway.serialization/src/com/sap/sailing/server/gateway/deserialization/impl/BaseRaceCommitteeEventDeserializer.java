package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BaseRaceCommitteeEventSerializer;

/// TODO deserialize involved boats
public abstract class BaseRaceCommitteeEventDeserializer implements JsonDeserializer<RaceCommitteeEvent> {

	protected abstract RaceCommitteeEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
			throws JsonDeserializationException;
	
	@Override
	public RaceCommitteeEvent deserialize(JSONObject object)
			throws JsonDeserializationException {
		// Factory handles class field and subclassing...
		String id = object.get(BaseRaceCommitteeEventSerializer.FIELD_ID).toString();
		long timeStamp = (Long) object.get(BaseRaceCommitteeEventSerializer.FIELD_TIMESTAMP);
		int passId = (Integer) object.get(BaseRaceCommitteeEventSerializer.FIELD_PASS_ID);
		
		return deserialize(object, Helpers.tryUuidConversion(id), new MillisecondsTimePoint(timeStamp), passId);
	}

}
