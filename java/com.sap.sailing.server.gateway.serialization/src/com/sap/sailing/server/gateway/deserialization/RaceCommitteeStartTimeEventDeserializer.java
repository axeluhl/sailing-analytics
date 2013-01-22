package com.sap.sailing.server.gateway.deserialization;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeStartTimeEventImpl;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeStartTimeEventSerializer;

public class RaceCommitteeStartTimeEventDeserializer extends RaceCommitteeEventDeserializer {

	@Override
	protected RaceCommitteeEvent deserialize(JSONObject object, UUID id,
			TimePoint timePoint, int passId) {
		
		long startTime = (Long) object.get(RaceCommitteeStartTimeEventSerializer.FIELD_START_TIME);
		
		return new RaceCommitteeStartTimeEventImpl(
				timePoint, 
				id, 
				Collections.<Competitor>emptyList(), 
				passId, 
				new MillisecondsTimePoint(startTime));
	}


}
