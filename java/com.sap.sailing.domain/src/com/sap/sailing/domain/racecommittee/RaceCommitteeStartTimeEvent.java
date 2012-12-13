package com.sap.sailing.domain.racecommittee;

import com.sap.sailing.domain.common.TimePoint;

public interface RaceCommitteeStartTimeEvent extends RaceCommitteeEvent {
	
	TimePoint getStartTime();
}
