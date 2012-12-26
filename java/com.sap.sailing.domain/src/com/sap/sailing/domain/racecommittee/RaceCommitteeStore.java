package com.sap.sailing.domain.racecommittee;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;


/**
 * Capsulating store interface providing a {@link RaceCommitteeEventTrack} for a given regatta / race combination.
 *
 */
public interface RaceCommitteeStore {
	RaceCommitteeEventTrack getRaceCommitteeEventTrack(TrackedRegatta trackedRegatta, TrackedRace trackedRace);
}
