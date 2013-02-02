package com.sap.sailing.domain.racecommittee.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeCourseAreaChangedEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventFactory;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

public class RaceCommitteeEventFactoryImpl implements RaceCommitteeEventFactory {

	@Override
	public RaceCommitteeFlagEvent createFlagEvent(TimePoint timePoint,
			Serializable id, List<Competitor> involvedBoats, int passId,
			Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return new RaceCommitteeFlagEventImpl(timePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
	}
	
	@Override
	public RaceCommitteeFlagEvent createFlagEvent(TimePoint timePoint,
			int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
		return createFlagEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag, lowerFlag, isDisplayed);
	}

	@Override
	public RaceCommitteeStartTimeEvent createStartTimeEvent(
			TimePoint timePoint, Serializable id,
			List<Competitor> involvedBoats, int passId, TimePoint startTime) {
		return new RaceCommitteeStartTimeEventImpl(timePoint, id, involvedBoats, passId, startTime);
	}

	@Override
	public RaceCommitteeStartTimeEvent createStartTimeEvent(
			TimePoint timePoint, int passId, TimePoint startTime) {
		return createStartTimeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
	}

	@Override
	public RaceCommitteeCourseAreaChangedEvent createCourseAreaChangedEvent(
			TimePoint timePoint, Serializable id,
			List<Competitor> involvedBoats, int passId,
			Serializable courseAreaId) {
		return new RaceCommitteeCourseAreaChangeEventImpl(timePoint, id, involvedBoats, passId, courseAreaId);
	}

	@Override
	public RaceCommitteeCourseAreaChangedEvent createCourseAreaChangedEvent(
			TimePoint timePoint, int passId, Serializable courseAreaId) {
		return createCourseAreaChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, courseAreaId);
	}

}
