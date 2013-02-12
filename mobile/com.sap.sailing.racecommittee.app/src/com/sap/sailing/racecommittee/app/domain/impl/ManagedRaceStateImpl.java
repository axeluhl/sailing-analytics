package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceState;

public class ManagedRaceStateImpl implements ManagedRaceState {

	protected RaceLog raceLog;
	
	public ManagedRaceStateImpl(RaceLog raceLog) {
		this.raceLog = raceLog;
	}

	public RaceLog getRaceLog() {
		return raceLog;
	}

	public RaceLogRaceStatus getStatus() {
		return RaceLogRaceStatus.UNKNOWN;
	}

	public RaceLogRaceStatus updateStatus() {
		/// TODO: implement
		return getStatus();
	}

}
