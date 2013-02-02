package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

	@Override
	public RaceLogFlagEvent createFlagEvent(TimePoint timePoint,
			Serializable id, List<Competitor> involvedBoats, int passId,
			Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return new RaceLogFlagEventImpl(timePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
	}
	
	@Override
	public RaceLogFlagEvent createFlagEvent(TimePoint timePoint,
			int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return createFlagEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag, lowerFlag, isDisplayed);
	}

	@Override
	public RaceLogStartTimeEvent createStartTimeEvent(
			TimePoint timePoint, Serializable id,
			List<Competitor> involvedBoats, int passId, TimePoint startTime) {
		return new RaceLogStartTimeEventImpl(timePoint, id, involvedBoats, passId, startTime);
	}

	@Override
	public RaceLogStartTimeEvent createStartTimeEvent(
			TimePoint timePoint, int passId, TimePoint startTime) {
		return createStartTimeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
	}

}
