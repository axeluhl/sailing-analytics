package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeEventSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeStartTimeEventSerializer;

public class RaceCommitteeEventDeserializerFactory {

	protected JsonDeserializer<RaceCommitteeEvent> flagEventDeserializer;
	protected JsonDeserializer<RaceCommitteeEvent> startTimeEventDeserializer;

	public RaceCommitteeEventDeserializerFactory() {
		flagEventDeserializer = new RaceCommitteeFlagEventDeserializer();
		startTimeEventDeserializer = new RaceCommitteeStartTimeEventDeserializer();
	}

	public JsonDeserializer<RaceCommitteeEvent> getDeserializer(
			JSONObject object) throws JsonDeserializationException {
		Object type = object.get(RaceCommitteeEventSerializer.FIELD_CLASS);

		if (type.equals(RaceCommitteeFlagEventSerializer.VALUE_CLASS)) {
			return flagEventDeserializer;
		} else if (type.equals(RaceCommitteeStartTimeEventSerializer.VALUE_CLASS)) {
			return startTimeEventDeserializer;
		}

		throw new JsonDeserializationException(String.format(
				"There is no deserializer defined for event type %s.", type));
	}
}