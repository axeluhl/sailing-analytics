package com.sap.sailing.server.gateway.serialization;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

public class RaceCommitteeEventSerializerFactory {
	
	private JsonSerializer<RaceCommitteeEvent> baseEventSerializer;
	
	public RaceCommitteeEventSerializerFactory() {
		this.baseEventSerializer = new RaceCommitteeEventSerializer();
	}
	
	public JsonSerializer<RaceCommitteeEvent> getSerializer(RaceCommitteeEvent event) {
		if (event instanceof RaceCommitteeFlagEvent) {
			return new RaceCommitteeFlagEventSerializer(baseEventSerializer);
		}
		if (event instanceof RaceCommitteeStartTimeEvent) {
			return new RaceCommitteeStartTimeEventSerializer(baseEventSerializer);
		}
		throw new UnsupportedOperationException(
				String.format(
						"There is no serializer defined for event type %s", 
						event.getClass().getName()));
	}
	
}
