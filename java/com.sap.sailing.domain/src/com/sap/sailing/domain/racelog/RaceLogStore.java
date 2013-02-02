package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;


/**
 * Capsulating store interface providing a {@link RaceLog} for a given regatta / race combination.
 *
 */
public interface RaceLogStore {
	RaceLog getRaceLog(TrackedRegatta trackedRegatta, TrackedRace trackedRace);
}
