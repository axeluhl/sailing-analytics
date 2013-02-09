package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.RaceLog;

public interface ManagedRace extends ManagedRaceIdentifier, Named, WithID {
	
	public ManagedRaceIdentifier getIdentifier();

	public RaceStatus getStatus();
	
	public RaceLog getRaceLog();
	
}
