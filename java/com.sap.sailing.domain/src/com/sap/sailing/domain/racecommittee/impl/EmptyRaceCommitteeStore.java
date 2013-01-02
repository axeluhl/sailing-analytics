package com.sap.sailing.domain.racecommittee.impl;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

/**
 * Since the RaceCommitteeStore is part of the TrackedRace constructor and not all TrackedRace instances need a RaceCommitteeStore (e.g. tests)
 * this dummy implementation is provided.
 *
 */
public class EmptyRaceCommitteeStore implements RaceCommitteeStore {
	public static EmptyRaceCommitteeStore INSTANCE = new EmptyRaceCommitteeStore();
	
	private RaceCommitteeEventTrack track = null;

	@Override
	public RaceCommitteeEventTrack getRaceCommitteeEventTrack(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
		if (track == null) {
			track = new RaceCommitteeEventTrackImpl("Lock for " + trackedRace.toString());
		}
		
		return track;
	}
}
