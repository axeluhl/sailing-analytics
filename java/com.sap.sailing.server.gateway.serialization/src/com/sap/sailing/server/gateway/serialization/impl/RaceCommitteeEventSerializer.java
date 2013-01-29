package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCommitteeEventSerializer implements JsonSerializer<RaceCommitteeEvent> {

	private final JsonSerializer<RaceCommitteeEvent> flagEventSerializer;
	private final JsonSerializer<RaceCommitteeEvent> startTimeSerializer;

	public RaceCommitteeEventSerializer(
			JsonSerializer<RaceCommitteeEvent> flagEventSerializer,
			JsonSerializer<RaceCommitteeEvent> startTimeSerializer) {
		this.flagEventSerializer = flagEventSerializer;
		this.startTimeSerializer = startTimeSerializer;
	}

	protected JsonSerializer<RaceCommitteeEvent> getSerializer(
			RaceCommitteeEvent event) {
		if (event instanceof RaceCommitteeFlagEvent) {
			return flagEventSerializer;
		} else if (event instanceof RaceCommitteeStartTimeEvent) {
			return startTimeSerializer;
		}

		throw new UnsupportedOperationException(String.format(
				"There is no serializer defined for event type %s", event
						.getClass().getName()));
	}

	@Override
	public JSONObject serialize(RaceCommitteeEvent object) {
		return getSerializer(object).serialize(object);
	}

}
