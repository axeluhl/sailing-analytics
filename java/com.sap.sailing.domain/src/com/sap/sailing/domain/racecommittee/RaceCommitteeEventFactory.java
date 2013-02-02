package com.sap.sailing.domain.racecommittee;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeEventFactoryImpl;

public interface RaceCommitteeEventFactory {
	RaceCommitteeEventFactory INSTANCE = new RaceCommitteeEventFactoryImpl();
	
	RaceCommitteeFlagEvent createFlagEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);
	RaceCommitteeFlagEvent createFlagEvent(TimePoint timePoint, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);
	
	RaceCommitteeStartTimeEvent createStartTimeEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime);
	RaceCommitteeStartTimeEvent createStartTimeEvent(TimePoint timePoint, int passId, TimePoint startTime);
	
	RaceCommitteeCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, Serializable courseAreaId);
	RaceCommitteeCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, int passId, Serializable courseAreaId);

}
