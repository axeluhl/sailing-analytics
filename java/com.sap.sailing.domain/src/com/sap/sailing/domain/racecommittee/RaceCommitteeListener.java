package com.sap.sailing.domain.racecommittee;

public interface RaceCommitteeListener {
	void flagEventReceived(RaceCommitteeFlagEvent flagEvent);
	
	void startTimeEventReceived(RaceCommitteeStartTimeEvent startTimeEvent);
}
