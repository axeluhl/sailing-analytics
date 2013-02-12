package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;

public interface ManagedRaceState {

	/**
	 * Updates the race's status.
	 * @return the new status, as returned by {@link ManagedRaceState#getStatus()}.
	 */
	RaceLogRaceStatus updateStatus();
	
	/**
	 * @return the status of the race.
	 */
	RaceLogRaceStatus getStatus();	
	
	/**
	 * @return the log of the race.
	 */
	public RaceLog getRaceLog();
	
}
