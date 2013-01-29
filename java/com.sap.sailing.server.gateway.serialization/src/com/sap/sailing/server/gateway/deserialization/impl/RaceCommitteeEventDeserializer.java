package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BaseRaceCommitteeEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeStartTimeEventSerializer;

public class RaceCommitteeEventDeserializer implements JsonDeserializer<RaceCommitteeEvent> {

	protected JsonDeserializer<RaceCommitteeEvent> flagEventDeserializer;
	protected JsonDeserializer<RaceCommitteeEvent> startTimeEventDeserializer;

	public RaceCommitteeEventDeserializer() {
		flagEventDeserializer = new RaceCommitteeFlagEventDeserializer();
		startTimeEventDeserializer = new RaceCommitteeStartTimeEventDeserializer();
	}

	protected JsonDeserializer<RaceCommitteeEvent> getDeserializer(
			JSONObject object) throws JsonDeserializationException {
		Object type = object.get(BaseRaceCommitteeEventSerializer.FIELD_CLASS);

		if (type.equals(RaceCommitteeFlagEventSerializer.VALUE_CLASS)) {
			return flagEventDeserializer;
		} else if (type.equals(RaceCommitteeStartTimeEventSerializer.VALUE_CLASS)) {
			return startTimeEventDeserializer;
		}

		throw new JsonDeserializationException(String.format(
				"There is no deserializer defined for event type %s.", type));
	}

	@Override
	public RaceCommitteeEvent deserialize(JSONObject object)
			throws JsonDeserializationException {
		return getDeserializer(object).deserialize(object);
	}
}