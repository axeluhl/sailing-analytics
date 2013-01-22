package com.sap.sailing.server.gateway.deserialization;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeFlagEventImpl;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeFlagEventSerializer;

public class RaceCommitteeFlagEventDeserializer extends
		RaceCommitteeEventDeserializer {

	@Override
	protected RaceCommitteeEvent deserialize(JSONObject object, UUID id,
			TimePoint timePoint, int passId) {

		Flags upperFlag = Flags.valueOf(object.get(
				RaceCommitteeFlagEventSerializer.FIELD_UPPER_FLAG).toString());
		Flags lowerFlag = Flags.valueOf(object.get(
				RaceCommitteeFlagEventSerializer.FIELD_LOWER_FLAG).toString());
		boolean isDisplayed = (Boolean) object
				.get(RaceCommitteeFlagEventSerializer.FIELD_DISPLAYED);
		
		return new RaceCommitteeFlagEventImpl(timePoint, id,
				Collections.<Competitor>emptyList(), passId, upperFlag,
				lowerFlag, isDisplayed);
	}

}
