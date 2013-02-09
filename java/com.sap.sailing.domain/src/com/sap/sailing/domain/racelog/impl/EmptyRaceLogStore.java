package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;

/**
 * Since the RaceLogStore is part of the TrackedRace constructor and not all TrackedRace instances need a RaceLogStore (e.g. tests)
 * this dummy implementation is provided.
 *
 */
public class EmptyRaceLogStore implements RaceLogStore {
	public static EmptyRaceLogStore INSTANCE = new EmptyRaceLogStore();
	
	@Override
	public RaceLog getRaceLog(RaceColumnIdentifier identifier) {
		return new RaceLogImpl("Lock for " + identifier.getIdentifier().toString());
	}
}
