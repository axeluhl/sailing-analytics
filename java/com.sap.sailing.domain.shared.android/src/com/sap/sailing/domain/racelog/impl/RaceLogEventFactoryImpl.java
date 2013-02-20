package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

	@Override
	public RaceLogFlagEvent createFlagEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId,
			Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return new RaceLogFlagEventImpl(timePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
	}
	
	@Override
	public RaceLogFlagEvent createFlagEvent(TimePoint timePoint,
			int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return createFlagEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag, lowerFlag, isDisplayed);
	}

	@Override
	public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, 
			RaceLogRaceStatus nextStatus, TimePoint startTime) {
		return new RaceLogStartTimeEventImpl(timePoint, id, involvedBoats, passId, nextStatus, startTime);
	}

	@Override
	public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, int passId, RaceLogRaceStatus nextStatus, TimePoint startTime) {
		return createStartTimeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus, startTime);
	}

	@Override
	public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint,Serializable id, List<Competitor> competitors, int passId,
			RaceLogRaceStatus nextStatus) {
		return new RaceLogRaceStatusEventImpl(timePoint, id, competitors, passId, nextStatus);
	}

	@Override
	public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint,
			int passId, RaceLogRaceStatus nextStatus) {
		return createRaceStatusEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus);
	}

	@Override
	public RaceLogPassChangeEvent createRaceLogPassChangeEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors,
			int passId) {
		return new RaceLogPassChangeEventImpl(timePoint, id, competitors, passId);
	}

	@Override
	public RaceLogPassChangeEvent createRaceLogPassChangeEvent(TimePoint timePoint, int passId) {
		return createRaceLogPassChangeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
	}

	@Override
	public RaceLogCourseAreaChangedEvent createRaceLogCourseAreaChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors,
			int passId, Serializable courseAreaId) {
		return new RaceLogCourseAreaChangeEventImpl(timePoint, id, competitors, passId, courseAreaId);
	}

	@Override
	public RaceLogCourseAreaChangedEvent createRaceLogCourseAreaChangedEvent(
			TimePoint timePoint, int passId, Serializable courseAreaId) {
		return createRaceLogCourseAreaChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, courseAreaId);
	}

}
