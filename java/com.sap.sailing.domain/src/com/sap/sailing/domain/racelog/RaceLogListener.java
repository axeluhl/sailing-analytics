package com.sap.sailing.domain.racelog;

public interface RaceLogListener {
	void flagEventReceived(RaceLogFlagEvent flagEvent);
	
	void startTimeEventReceived(RaceLogStartTimeEvent startTimeEvent);
}
