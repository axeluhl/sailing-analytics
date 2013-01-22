package com.sap.sailing.server.gateway.serialization;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

public class RaceCommitteeEventSerializerFactory {

	private JsonSerializer<RaceCommitteeEvent> flagEventSerializer;
	private JsonSerializer<RaceCommitteeEvent> startTimeSerializer;

	public RaceCommitteeEventSerializerFactory() {
		flagEventSerializer = new RaceCommitteeFlagEventSerializer();
		startTimeSerializer = new RaceCommitteeStartTimeEventSerializer();
	}

	public JsonSerializer<RaceCommitteeEvent> getSerializer(
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

}
