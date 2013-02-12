package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceState;
import com.sap.sailing.racecommittee.app.domain.RaceStatus;

public class ManagedRaceStateImpl implements ManagedRaceState {

	protected RaceLog raceLog;
	
	public ManagedRaceStateImpl(RaceLog raceLog) {
		this.raceLog = raceLog;
	}

	public RaceLog getRaceLog() {
		return raceLog;
	}

	public RaceStatus getStatus() {
		return RaceStatus.UNKNOWN;
	}

	public RaceStatus updateStatus() {
		
		return getStatus();
	}

}
