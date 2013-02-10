package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Fleet;

/**
 * Capsulating store interface providing a {@link RaceLog} for a given regatta / race combination.
 *
 */
public interface RaceLogStore {
	RaceLog getRaceLog(Fleet fleet);

	RaceLog getRaceLog(RaceColumnIdentifier identifier, Fleet fleet);
}
