package com.sap.sailing.domain.racecommittee;

public interface RaceCommitteeFlagEvent extends RaceCommitteeEvent {
	
	Flags getUpperFlag();
	
	Flags getLowerFlag();
	
	boolean isDisplayed();
}
