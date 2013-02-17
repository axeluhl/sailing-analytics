package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;

public interface RaceState {

	void registerListener(RaceStateChangedListener listener);
	void unregisterListener(RaceStateChangedListener listener);
	
	/**
	 * Updates the race's status.
	 * @return the new status, as returned by {@link RaceState#getStatus()}.
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
