package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

/**
 * Since the RaceLogStore is part of the TrackedRace constructor and not all TrackedRace instances need a RaceLogStore (e.g. tests)
 * this dummy implementation is provided.
 *
 */
public class EmptyRaceLogStore implements RaceLogStore {
	public static EmptyRaceLogStore INSTANCE = new EmptyRaceLogStore();
	
	@Override
	public RaceLog getRaceLog(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
		return new RaceLogImpl("Lock for " + trackedRace.toString());
	}
}
