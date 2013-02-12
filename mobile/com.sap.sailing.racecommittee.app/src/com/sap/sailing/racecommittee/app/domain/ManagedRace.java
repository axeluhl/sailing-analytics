package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.domain.state.ManagedRaceState;

public interface ManagedRace extends ManagedRaceIdentifier, Named, WithID {
	
	/**
	 * @return the identifier of the race.
	 */
	public ManagedRaceIdentifier getIdentifier();
	

	/**
	 * @return the state of the race.
	 */
	public ManagedRaceState getState();

	/**
	 * Shortcut to {@link ManagedRaceState#getRaceLog()} of {@link ManagedRace#getState()}.
	 * @return the log of the race.
	 */
	public RaceLog getRaceLog();
	
	/**
	 * Shortcut to {@link ManagedRaceState#getStatus()} of {@link ManagedRace#getState()}.
	 * @return the status of the race's state.
	 */
	public RaceLogRaceStatus getStatus();
	
}
