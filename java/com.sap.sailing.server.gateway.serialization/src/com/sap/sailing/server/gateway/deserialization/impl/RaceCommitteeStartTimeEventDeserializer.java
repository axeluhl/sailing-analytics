package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeStartTimeEventImpl;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeStartTimeEventSerializer;

public class RaceCommitteeStartTimeEventDeserializer extends BaseRaceCommitteeEventDeserializer {

	@Override
	protected RaceCommitteeEvent deserialize(JSONObject object, Serializable id,
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
