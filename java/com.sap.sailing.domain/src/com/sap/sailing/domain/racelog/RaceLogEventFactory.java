package com.sap.sailing.domain.racelog;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;

public interface RaceLogEventFactory {
	RaceLogEventFactory INSTANCE = new RaceLogEventFactoryImpl();
	
	RaceLogFlagEvent createFlagEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);
	RaceLogFlagEvent createFlagEvent(TimePoint timePoint, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);
	
	RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime);
	RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, int passId, TimePoint startTime);

}
