package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.server.gateway.serialization.impl.RaceLogStartTimeEventSerializer;

public class RaceLogStartTimeEventDeserializer extends BaseRaceLogEventDeserializer {

	@Override
	protected RaceLogEvent deserialize(JSONObject object, UUID id,
			TimePoint timePoint, int passId) {
		
		long startTime = (Long) object.get(RaceLogStartTimeEventSerializer.FIELD_START_TIME);
		
		return new RaceLogStartTimeEventImpl(
				timePoint, 
				id, 
				Collections.<Competitor>emptyList(), 
				passId, 
				new MillisecondsTimePoint(startTime));
	}


}
