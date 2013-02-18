package com.sap.sailing.domain.racelog;

public interface RaceLogRaceStatusEvent extends RaceLogEvent {
	
	RaceLogRaceStatus getNextStatus();

}
